package me.jacoblewis.socialmediaapi.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

@JsonInclude(NON_NULL)
data class RequestReceipt<T>(
    val status: Int,
    val message: String?,
    val body: T? = null
)