package me.jacoblewis.socialmediaapi.utils.authfilter

import me.jacoblewis.socialmediaapi.utils.Authorities

@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthFilter(val authorities: Array<Authorities>)