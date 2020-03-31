package me.jacoblewis.socialmediaapi.config

import me.jacoblewis.socialmediaapi.repos.UserRepository
import me.jacoblewis.socialmediaapi.utils.AntPathRequestMatcherWrapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2RefreshToken
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import java.util.*
import javax.servlet.http.HttpServletRequest

@Configuration
@EnableResourceServer
class OAuthSecurityConfig : ResourceServerConfigurerAdapter() {

    @Autowired
    lateinit var userRepo: UserRepository

    override fun configure(http: HttpSecurity) {
        http
            .requestMatcher(object : AntPathRequestMatcherWrapper(
                "/users/**",
                "/following",
                "/posts/**"
            ) {
                override fun precondition(request: HttpServletRequest): Boolean {
                    return true
                }
            })
            .authorizeRequests()
            .requestMatchers(
                AntPathRequestMatcher("/users/**"),
                AntPathRequestMatcher("/following"),
                AntPathRequestMatcher("/posts/**")
            )
            .authenticated()
    }

    override fun configure(resources: ResourceServerSecurityConfigurer) {
        resources.tokenServices(object : ResourceServerTokenServices {
            override fun loadAuthentication(accessToken: String?): OAuth2Authentication? {
                println("LOAD: $accessToken")

                val user = userRepo.findByToken(accessToken ?: "").firstOrNull() ?: return null
                // If user is not null, create a token!
                val authorities = user.authorities?.map { GrantedAuthority { it } } ?: listOf()
                return OAuth2Authentication(
                    OAuth2Request(
                        mutableMapOf(), "test", authorities, true, mutableSetOf(),
                        mutableSetOf("test"), "", mutableSetOf(), mutableMapOf()
                    ), object : Authentication {
                        override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
                            return authorities.toMutableList()
                        }

                        override fun setAuthenticated(isAuthenticated: Boolean) {
                        }

                        override fun getName(): String {
                            return user.id ?: ""
                        }

                        override fun getCredentials(): Any {
                            return user.email ?: ""
                        }

                        override fun getPrincipal(): Any {
                            return user.id ?: ""
                        }

                        override fun isAuthenticated(): Boolean {
                            return true
                        }

                        override fun getDetails(): Any {
                            return user.toString()
                        }

                    })
            }

            override fun readAccessToken(accessToken: String?): OAuth2AccessToken {
                println("READ: $accessToken")

                return object : OAuth2AccessToken {
                    override fun isExpired(): Boolean {
                        return false
                    }

                    override fun getExpiresIn(): Int {
                        return 1000
                    }

                    override fun getExpiration(): Date {
                        return Date()
                    }

                    override fun getAdditionalInformation(): MutableMap<String, Any> {
                        return mutableMapOf()
                    }

                    override fun getTokenType(): String {
                        return "BEARER"
                    }

                    override fun getScope(): MutableSet<String> {
                        return mutableSetOf("STANDARD")
                    }

                    override fun getValue(): String {
                        return "TOKEN"
                    }

                    override fun getRefreshToken(): OAuth2RefreshToken {
                        return OAuth2RefreshToken { "" }
                    }

                }
            }

        }).resourceId("test")
    }
}