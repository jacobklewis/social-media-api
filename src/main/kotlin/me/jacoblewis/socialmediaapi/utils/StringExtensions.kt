package me.jacoblewis.socialmediaapi.utils

val String?.isOn: Boolean
    get() = this?.equals("on", ignoreCase = true) == true || this?.equals("true", ignoreCase = true) == true
