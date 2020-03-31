package me.jacoblewis.socialmediaapi.utils

import java.util.*

val <T> Optional<T>.value: T?
    get() = if (isPresent) get() else null