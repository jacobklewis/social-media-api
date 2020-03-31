package me.jacoblewis.socialmediaapi.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnAuthorizedException (override val message: String?) : SocialMediaException(message)