package me.jacoblewis.socialmediaapi.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException (override val message: String?) : SocialMediaException(message)