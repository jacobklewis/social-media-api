package me.jacoblewis.socialmediaapi.models

data class ConversationMessageModel(
    val id: String,
    val members: List<UserModel>,
    val name: String,
    val messages: List<MessageModel>
)