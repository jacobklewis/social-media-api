package me.jacoblewis.socialmediaapi.models

import org.springframework.data.annotation.Id
import java.util.*

data class PostModel(
    @Id
    val id: String,
    val creatorId: String,
    val creationDate: Date,
    var title: String,
    var content: String?,
    var likes: List<String>?
)