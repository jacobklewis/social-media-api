package me.jacoblewis.socialmediaapi.utils

import java.util.*

private val b64Enc = Base64.getEncoder().withoutPadding()
private val b64Dec = Base64.getDecoder()

fun b64EncStr(string: String?): String {
    return b64Enc.encodeToString(string?.toByteArray() ?: ByteArray(0))
}

fun b64DecStr(b64: String?): String {
    return b64Dec.decode(b64)?.contentToString() ?: ""
}