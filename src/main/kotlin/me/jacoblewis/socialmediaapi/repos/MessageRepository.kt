package me.jacoblewis.socialmediaapi.repos

import me.jacoblewis.socialmediaapi.models.MessageModel
import org.springframework.data.mongodb.repository.MongoRepository

interface MessageRepository : MongoRepository<MessageModel, String> {
    fun findByConversationId(conversationId: String): List<MessageModel>

    fun findTopByConversationIdOrderByCreationDateDesc(conversationId: String): MessageModel?
}