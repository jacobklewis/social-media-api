package me.jacoblewis.socialmediaapi.utils

import org.springframework.security.core.context.SecurityContextHolder

val currentAuthorities: List<String>
    get() = SecurityContextHolder.getContext().authentication.authorities.map { it.authority }

val currentUserID: String
    get() = SecurityContextHolder.getContext().authentication.principal.toString()