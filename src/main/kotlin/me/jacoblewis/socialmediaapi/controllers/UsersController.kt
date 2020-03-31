package me.jacoblewis.socialmediaapi.controllers

import io.swagger.annotations.ApiOperation
import me.jacoblewis.socialmediaapi.exceptions.ConflictException
import me.jacoblewis.socialmediaapi.exceptions.NotFoundException
import me.jacoblewis.socialmediaapi.models.RequestReceipt
import me.jacoblewis.socialmediaapi.models.UserModel
import me.jacoblewis.socialmediaapi.repos.UserRepository
import me.jacoblewis.socialmediaapi.utils.authfilter.filterByAuth
import me.jacoblewis.socialmediaapi.utils.b64EncStr
import me.jacoblewis.socialmediaapi.utils.currentUserID
import me.jacoblewis.socialmediaapi.utils.isOn
import me.jacoblewis.socialmediaapi.utils.value
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import javax.websocket.server.PathParam

@Component
@RestController
class UsersController {

    @Autowired
    lateinit var userRepo: UserRepository

    @GetMapping("/users/{userID}")
    @ApiOperation("Get User Details")
    fun getMyUser(@PathVariable("userID") userID: String,
    @RequestParam("detailed", required = false, defaultValue = "true") detailed: String): ResponseEntity<RequestReceipt<UserModel>> {
        val user = userRepo.findById(userID).value
            ?: throw NotFoundException("unable to find user with ID: `$userID`")
        if (detailed.isOn) {
            user.followingUsers =
                (user.followingUsers ?: listOf()).mapNotNull { userRepo.findById(it.id ?: "").value?.filterByAuth() }
        }
        return ResponseEntity.ok(RequestReceipt(200, "user found", user.filterByAuth()))
    }

    @GetMapping("/users")
    @ApiOperation("Search for Users")
    fun getUsers(@RequestParam("q", required = false) query: String?): ResponseEntity<List<UserModel>> {
        var filteredUsers = userRepo.findAll()
        if (query != null) {
            // Query
            filteredUsers = filteredUsers.filter {
                it.firstName?.contains(query, ignoreCase = true) == true ||
                        it.lastName?.contains(query, ignoreCase = true) == true ||
                        it.email?.contains(query, ignoreCase = true) == true
            }
        }
        // Filter out yourself and remove unneeded info
        filteredUsers = filteredUsers.filter { it.id != currentUserID }.map {
            it.followingUsers = null
            it.filterByAuth()
        }
        return ResponseEntity.ok(filteredUsers)
    }

    @PostMapping("/following", consumes = ["multipart/form-data"])
    @ApiOperation("Follow a User")
    fun followUser(@RequestPart("userID") userIDToFollow: String): ResponseEntity<RequestReceipt<UserModel>> {
        if (userIDToFollow == currentUserID) {
            throw ConflictException("Unable to follow yourself")
        }
        val currentUser = userRepo.findById(currentUserID).value
            ?: throw NotFoundException("unable to find current user")
        val userToFollow = userRepo.findById(userIDToFollow).value
            ?: throw NotFoundException("user to follow not found")
        val followCopy = userToFollow.createFollowCopy()
        if (currentUser.followingUsers?.contains(followCopy) == true) {
            throw ConflictException("'${currentUser.email}' is already following '${userToFollow.email}'")
        }
        currentUser.followingUsers = ((currentUser.followingUsers ?: listOf()) + listOf(followCopy)).toSet().toList()
        userRepo.save(currentUser)
        return ResponseEntity.status(201)
            .body(RequestReceipt(201, "'${currentUser.email}' is now following '${userToFollow.email}'", currentUser))
    }

    @DeleteMapping("/following", consumes = ["multipart/form-data"])
    @ApiOperation("Unfollow User")
    fun unfollowUser(@RequestPart("userID") userIDToFollow: String): ResponseEntity<RequestReceipt<UserModel>> {
        if (userIDToFollow == currentUserID) {
            throw ConflictException("Unable to unfollow yourself")
        }
        val currentUser = userRepo.findById(currentUserID).value
            ?: throw NotFoundException("unable to find current user")
        val userToFollow = userRepo.findById(userIDToFollow).value
            ?: throw NotFoundException("user to unfollow not found")
        val followCopy = userToFollow.createFollowCopy()
        if (currentUser.followingUsers?.contains(followCopy) == true) {
            currentUser.followingUsers = ((currentUser.followingUsers ?: listOf()) - listOf(followCopy)).toSet().toList()
            userRepo.save(currentUser)
            return ResponseEntity.ok(RequestReceipt(200, "'${currentUser.email}' is now unfollowed to '${userToFollow.email}'", currentUser))
        } else {
            return ResponseEntity.status(304).body(RequestReceipt(304, "'${currentUser.email}' is already not following '${userToFollow.email}'", currentUser))
        }
    }
}