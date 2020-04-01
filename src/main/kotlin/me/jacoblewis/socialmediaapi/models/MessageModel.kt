package me.jacoblewis.socialmediaapi.models

import org.springframework.data.annotation.Id
import java.util.*

data class MessageModel(
    @Id
    val id: String,
    val conversationId: String,
    val creationDate: Date,
    var userId: String,
    var text: String?
)