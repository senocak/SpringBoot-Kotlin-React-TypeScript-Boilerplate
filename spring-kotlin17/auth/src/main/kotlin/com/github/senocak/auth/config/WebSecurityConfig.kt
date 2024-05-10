package com.github.senocak.auth.config

import com.github.senocak.auth.security.JwtAuthenticationEntryPoint
import com.github.senocak.auth.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val unauthorizedHandler: JwtAuthenticationEntryPoint,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    /**
     * Override this method to configure the HttpSecurity.
     * @param http -- It allows configuring web based security for specific http requests
     * @throws Exception -- throws Exception
     */
    //@Profile("!integration-test")
    @Throws(Exception::class)
    @Bean
    fun securityFilterChainDSL(http: HttpSecurity): SecurityFilterChain =
        http {
            //cors {
            //    configurationSource = UrlBasedCorsConfigurationSource()
            //        .also { ubccs: UrlBasedCorsConfigurationSource ->
            //            ubccs.registerCorsConfiguration("/**", CorsConfiguration()
            //                .also {cc: CorsConfiguration ->
            //                    cc.allowCredentials = true
            //                    cc.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            //                    cc.allowedOrigins = listOf("https://example.com")
            //                    cc.allowedHeaders = listOf("Authorization", "Origin", "Content-Type", "Accept")
            //                    cc.exposedHeaders = listOf("Content-Type", "X-Rate-Limit-Retry-After-Seconds", "X-Rate-Limit-Remaining")
            //                })
            //        }
            //}
            //cors { Customizer.withDefaults<CorsConfiguration>() }
            csrf { disable() }
            exceptionHandling { authenticationEntryPoint = unauthorizedHandler }
            //httpBasic {}
            authorizeRequests {
                authorize(pattern = "/api/v1/auth/**", access = permitAll)
                authorize(pattern = "/api/v1/public/**", access = permitAll)
                authorize(pattern = "/api/v1/swagger/**", access = permitAll)
                authorize(pattern = "/swagger**/**", access = permitAll)
                authorize(pattern = "/ws/**", access = permitAll)
                authorize(pattern = "/api/v1/sse**/**", access = permitAll)
                authorize(pattern = "/*.html", access = permitAll)
                authorize(pattern = "/graphql/v1", access = permitAll)
                authorize(method = HttpMethod.GET, pattern = "/api/v1/ping", access = permitAll)
                //authorize(matches = PathRequest.toH2Console(), access = permitAll)
                //authorize(matches = CorsUtils::isPreFlightRequest, access = permitAll)
                authorize(matches = anyRequest, access = authenticated)
            }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            headers { frameOptions { disable() } }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(filter = jwtAuthenticationFilter)
        }
        .run { http.build() }

    //@Bean
    fun corsConfigurationSource(): CorsConfigurationSource =
        CorsConfiguration()
            .also {
                it.allowCredentials = true
                it.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
                it.allowedOrigins = listOf("https://example.com")
                it.allowedHeaders = listOf("Authorization", "Origin", "Content-Type", "Accept")
                it.exposedHeaders = listOf("Content-Type", "X-Rate-Limit-Retry-After-Seconds", "X-Rate-Limit-Remaining")
            }
            .run {
                UrlBasedCorsConfigurationSource()
                    .also { it.registerCorsConfiguration("/**", this) }
            }
}
