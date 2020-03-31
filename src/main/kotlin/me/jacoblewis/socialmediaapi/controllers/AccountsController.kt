package me.jacoblewis.socialmediaapi.controllers

import io.swagger.annotations.ApiKeyAuthDefinition
import io.swagger.annotations.ApiOperation
import me.jacoblewis.socialmediaapi.exceptions.BadRequestException
import me.jacoblewis.socialmediaapi.exceptions.ConflictException
import me.jacoblewis.socialmediaapi.exceptions.NotFoundException
import me.jacoblewis.socialmediaapi.exceptions.UnAuthorizedException
import me.jacoblewis.socialmediaapi.models.CreateUserModel
import me.jacoblewis.socialmediaapi.models.RequestReceipt
import me.jacoblewis.socialmediaapi.models.UserModel
import me.jacoblewis.socialmediaapi.repos.UserRepository
import me.jacoblewis.socialmediaapi.utils.Authorities
import me.jacoblewis.socialmediaapi.utils.b64EncStr
import me.jacoblewis.socialmediaapi.utils.value
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
import java.util.*

@Component
@RestController
class AccountsController {

    @Autowired
    lateinit var userRepo: UserRepository

    @PostMapping("/accounts", consumes = ["multipart/form-data"])
    @ApiOperation("Create an Account")
    fun createAccount(
        @RequestPart("firstName") firstName: String,
        @RequestPart("lastName") lastName: String,
        @RequestPart("email") email: String,
        @RequestPart("passwordHash") passwordHash: String): ResponseEntity<RequestReceipt<UserModel>> {
        val userID = b64EncStr(email + UUID.randomUUID().toString())
        val currentUser = userRepo.findById(userID).value
        // If null, then there is no current user
        if (currentUser == null) {
            val token = b64EncStr(UUID.randomUUID().toString())
            val user = UserModel(
                userID,
                firstName,
                lastName,
                email,
                passwordHash,
                token,
                listOf(Authorities.STANDARD.name),
                listOf()
            )
            userRepo.save(user)
            val receipt = RequestReceipt(201, "user created", user)
            return ResponseEntity.status(HttpStatus.CREATED).body(receipt)
        } else {
            throw ConflictException("User already Exists")
        }
    }

    @PostMapping("/authenticate", consumes = ["multipart/form-data"])
    @ApiOperation(value = "Login / Authenticate", consumes = "multipart/form-data", produces = "application/json")
    fun authenticate(
        @RequestPart("email") email: String,
        @RequestPart("passwordHash") passwordHash: String
    ): ResponseEntity<RequestReceipt<UserModel>> {
        val userID = b64EncStr(email)
        val currentUser =
            userRepo.findById(userID).value ?: throw NotFoundException("account with email: \"$email\" not found")
        if (currentUser.passwordHash != passwordHash) {
            throw UnAuthorizedException("either `email` or `passwordHash` is incorrect")
        }
        return ResponseEntity.ok(RequestReceipt(200, "authenticated", currentUser))
    }
}