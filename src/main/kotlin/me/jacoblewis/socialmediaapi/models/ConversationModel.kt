package me.jacoblewis.socialmediaapi.models

import org.springframework.data.annotation.Id

data class ConversationModel(
    @Id
    val id: String,
    var memberIds: List<String>,
    var name: String
)