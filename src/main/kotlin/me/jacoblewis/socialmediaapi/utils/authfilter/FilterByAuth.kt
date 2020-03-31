package me.jacoblewis.socialmediaapi.utils.authfilter

import me.jacoblewis.socialmediaapi.utils.currentAuthorities
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

fun <T : Any> T.filterByAuth(): T {
    val clazz = this::class
    val fields = clazz.memberProperties.filterIsInstance<KMutableProperty1<Any, Any?>>()
    for (field in fields) {
        field.findAnnotation<AuthFilter>()?.let {
            if (it.authorities.none { au -> currentAuthorities.contains(au.name) }) {
                field.set(this, null)
            }
        }
    }
    return this
}