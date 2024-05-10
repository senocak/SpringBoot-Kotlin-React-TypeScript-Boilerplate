package com.github.senocak.auth.service

import com.github.senocak.auth.domain.EmailActivationToken
import com.github.senocak.auth.domain.EmailActivationTokenRepository
import com.github.senocak.auth.domain.User
import com.github.senocak.auth.exception.ServerException
import com.github.senocak.auth.util.OmaErrorMessageType
import com.github.senocak.auth.util.randomStringGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.Date
import java.util.UUID

@Service
class EmailActivationTokenService(
    private val emailActivationTokenRepository: EmailActivationTokenRepository,
    private val messageSourceService: MessageSourceService,
    @Value("\${app.jwtExpirationInMs}") private val expiresIn: Long
) {

    /**
     * Is registration token expired?
     *
     * @param token EmailActivationToken
     * @return boolean
     */
    fun isRegistrationTokenExpired(token: EmailActivationToken): Boolean = token.expirationDate.before(Date())

    /**
     * Create email activation token from user.
     *
     * @param user User
     * @return EmailActivationToken
     */
    fun create(user: User): EmailActivationToken =
        EmailActivationToken(user = user, token = 15.randomStringGenerator())
            .also { it.expirationDate = Date.from(Instant.now().plusSeconds(expiresIn)) }
            .run { emailActivationTokenRepository.save(this) }

    fun findByUser(user: User): EmailActivationToken =
        emailActivationTokenRepository.findByUser(user = user)
            ?: throw ServerException(
                omaErrorMessageType = OmaErrorMessageType.NOT_FOUND,
                statusCode = HttpStatus.NOT_FOUND,
                variables = arrayOf(messageSourceService.get(code = "activation_token_not_found"))
            )

    fun findByToken(token: String): EmailActivationToken =
        emailActivationTokenRepository.findByToken(token = token)
            ?: throw ServerException(
                omaErrorMessageType = OmaErrorMessageType.NOT_FOUND,
                statusCode = HttpStatus.NOT_FOUND,
                variables = arrayOf(messageSourceService.get(code = "activation_token_not_found"))
            )

    /**
     * Get email activation token by token.
     * @param token String
     * @return User
     */
    fun getUserByToken(token: String): User =
        findByToken(token = token)
            .apply {
                if (isRegistrationTokenExpired(token = this)) {
                    throw ServerException(
                        omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                        statusCode = HttpStatus.BAD_REQUEST,
                        variables = arrayOf(messageSourceService.get(code = "registration_token_expired"))
                    )
                }
            }
            .run { this.user!! }

    /**
     * Delete email activation token by user ID.
     * @param userId String
     */
    @Transactional
    fun deleteByUserId(userId: UUID) {
        emailActivationTokenRepository.deleteByUserId(userId = userId)
    }
}
