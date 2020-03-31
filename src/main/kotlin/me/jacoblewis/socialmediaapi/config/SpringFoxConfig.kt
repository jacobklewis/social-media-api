package me.jacoblewis.socialmediaapi.config

import com.google.common.collect.Lists
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.SecurityConfiguration
import springfox.documentation.swagger.web.SecurityConfigurationBuilder
import springfox.documentation.swagger2.annotations.EnableSwagger2


@Configuration
@EnableSwagger2
class SpringFoxConfig {
    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("me.jacoblewis.socialmediaapi.controllers"))
            .paths(PathSelectors.regex("/.*"))
            .build()
            .enable(true)
            .securityContexts(listOf(securityContext()))
            .securitySchemes(listOf(apiKey()))
    }

    private fun apiKey(): ApiKey? {
        return ApiKey("apiKey", "Authorization", "header")
    }

    @Bean
    fun security(): SecurityConfiguration {
        return SecurityConfigurationBuilder.builder()
            .scopeSeparator(",")
            .additionalQueryStringParams(null)
            .useBasicAuthenticationWithAccessCodeGrant(false)
            .build()
    }

    private fun securityContext(): SecurityContext {
        return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .forPaths(PathSelectors.any())
            .build()
    }

    fun defaultAuth(): List<SecurityReference>? {
        val authorizationScope = AuthorizationScope("global", "accessEverything")
        val authorizationScopes: Array<AuthorizationScope?> = arrayOfNulls(1)
        authorizationScopes[0] = authorizationScope
        return Lists.newArrayList(
            SecurityReference("apiKey", authorizationScopes)
        )
    }
}