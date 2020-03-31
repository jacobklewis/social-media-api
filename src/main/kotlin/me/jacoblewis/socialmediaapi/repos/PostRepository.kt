package me.jacoblewis.socialmediaapi.repos

import me.jacoblewis.socialmediaapi.models.PostModel
import me.jacoblewis.socialmediaapi.models.UserModel
import org.springframework.data.mongodb.repository.MongoRepository

interface PostRepository : MongoRepository<PostModel, String> {
    fun findByCreatorId(creatorId: String): List<PostModel>
}