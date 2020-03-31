package me.jacoblewis.socialmediaapi.utils

import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.http.HttpServletRequest

abstract class AntPathRequestMatcherWrapper(vararg patterns: String) : RequestMatcher {

    private val delegate: List<AntPathRequestMatcher> = patterns.map { AntPathRequestMatcher(it) }

    override fun matches(request: HttpServletRequest): Boolean {
        return if (precondition(request)) {
            val match = delegate.any { it.matches(request) }
            match
        } else false
    }

    protected abstract fun precondition(request: HttpServletRequest): Boolean

}