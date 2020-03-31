package me.jacoblewis.socialmediaapi.exceptions

import java.lang.RuntimeException

open class SocialMediaException(override val message: String?) : RuntimeException(message)