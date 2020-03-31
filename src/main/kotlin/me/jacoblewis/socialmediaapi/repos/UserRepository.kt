package me.jacoblewis.socialmediaapi.repos

import me.jacoblewis.socialmediaapi.models.UserModel
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<UserModel, String> {

    fun findByToken(token: String): List<UserModel>
}