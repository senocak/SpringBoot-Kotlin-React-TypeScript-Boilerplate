package com.github.senocak.auth.service

import com.github.senocak.auth.domain.JwtToken
import com.github.senocak.auth.domain.PasswordResetToken
import com.github.senocak.auth.domain.UserEmailActivationSendEvent
import com.github.senocak.auth.util.logger
import org.slf4j.Logger
import org.slf4j.MDC
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.RedisKeyExpiredEvent
import org.springframework.data.repository.init.RepositoriesPopulatedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.support.RequestHandledEvent

@Component
@Async
class Listeners(
    private val emailService: EmailService,
    private val emailActivationTokenService: EmailActivationTokenService,
    private val webSocketCacheService: WebSocketCacheService,
){
    private val log: Logger by logger()

    @Transactional
    @EventListener(UserEmailActivationSendEvent::class)
    fun onUserRegisteredEvent(event: UserEmailActivationSendEvent): Unit =
        event.user
            .also { MDC.put("userId", "${event.user.id}") }
            .also { log.info("[UserRegisteredEvent] ${it.email} - ${it.id}") }
            .run { emailActivationTokenService.create(user = this) }
            .run { emailService.sendUserEmailActivation(user = user!!, emailActivationToken = this) }
            .also { MDC.remove("userId") }

    @EventListener(RedisKeyExpiredEvent::class)
    fun onRedisKeyExpiredEvent(event: RedisKeyExpiredEvent<Any?>): Unit =
        log.info("[RedisKeyExpiredEvent] $event")
            .run { event.value }
            .run {
                when {
                    this == null -> {
                        log.warn("Value is null, returning...")
                        return
                    }
                    else -> this
                }
            }
            .run {
                when (this.javaClass) {
                    JwtToken::class.java -> {
                        val jwtToken: JwtToken = this as JwtToken
                        log.info("Expired JwtToken: $jwtToken")
                        webSocketCacheService.deleteSession(key = jwtToken.email)
                    }
                    PasswordResetToken::class.java -> {
                        val passwordResetToken: PasswordResetToken = this as PasswordResetToken
                        MDC.put("userId", "${passwordResetToken.userId}")
                        log.info("Expired PasswordResetToken: $passwordResetToken")
                        MDC.remove("userId")
                    }
                    else -> log.warn("Unhandled event: ${this.javaClass} for redis...")
                }
            }

    @EventListener(RequestHandledEvent::class)
    fun onRequestHandledEvent(event: RequestHandledEvent): Unit = log.info("[RequestHandledEvent]: $event")

    @EventListener(RepositoriesPopulatedEvent::class)
    fun onRepositoriesPopulatedEvent(event: RepositoriesPopulatedEvent): Unit = log.info("[RepositoriesPopulatedEvent]: $event")
}
