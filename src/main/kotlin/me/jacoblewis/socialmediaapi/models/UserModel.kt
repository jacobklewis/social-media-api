package me.jacoblewis.socialmediaapi.models

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class UserModel(val name: String, val age: Int)