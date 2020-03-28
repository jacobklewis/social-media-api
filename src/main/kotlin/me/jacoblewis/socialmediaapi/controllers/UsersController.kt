package me.jacoblewis.socialmediaapi.controllers

import me.jacoblewis.socialmediaapi.models.UserModel
import me.jacoblewis.socialmediaapi.repos.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Component
@RestController
class UsersController {

    @Autowired
    lateinit var userRepo: UserRepository

    @GetMapping("/users/{query}")
    fun getUsers(@PathVariable("query") query: String): ResponseEntity<List<UserModel>> {
        val allUsers = userRepo.findAll()
        return ResponseEntity.ok(allUsers.filter { it.name.contains(query, ignoreCase = true) })
    }
}