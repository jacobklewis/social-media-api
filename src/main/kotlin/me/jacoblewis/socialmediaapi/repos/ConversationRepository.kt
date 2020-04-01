package me.jacoblewis.socialmediaapi.repos

import me.jacoblewis.socialmediaapi.models.ConversationModel
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface ConversationRepository : MongoRepository<ConversationModel, String> {
    @Query("{'memberIds': { \$all: [?0]} }")
    fun findConversationByMemberId(memberId: String): List<ConversationModel>
}