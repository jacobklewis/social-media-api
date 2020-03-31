package me.jacoblewis.socialmediaapi.models

data class CreateUserModel(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val passwordHash: String?
)