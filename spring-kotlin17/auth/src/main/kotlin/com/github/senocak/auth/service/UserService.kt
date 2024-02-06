package com.github.senocak.auth.service

import com.github.senocak.auth.domain.PasswordResetToken
import com.github.senocak.auth.domain.PasswordResetTokenRepository
import com.github.senocak.auth.domain.Role
import com.github.senocak.auth.domain.User
import com.github.senocak.auth.domain.UserRepository
import com.github.senocak.auth.domain.dto.ChangePasswordRequest
import com.github.senocak.auth.exception.ServerException
import com.github.senocak.auth.util.OmaErrorMessageType
import com.github.senocak.auth.util.RoleName
import com.github.senocak.auth.util.logger
import com.github.senocak.auth.util.randomStringGenerator
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import java.util.Date
import java.util.UUID
import org.slf4j.Logger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val messageSourceService: MessageSourceService,
    private val emailActivationTokenService: EmailActivationTokenService,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder
): UserDetailsService {
    private val log: Logger by logger()

    fun findAllUsers(specification: Specification<User>, pageRequest: Pageable): Page<User> =
        userRepository.findAll(specification, pageRequest)

    /**
     * @param id -- uuid to find in db
     * @return -- Optional User object
     */
    fun findById(id: UUID): User =
        userRepository.findById(id).orElseThrow { UsernameNotFoundException(messageSourceService.get(code = "user_not_found")) }

    /**
     * @param email -- string email to find in db
     * @return -- true or false
     */
    fun existsByEmail(email: String): Boolean =
        userRepository.existsByEmail(email = email)

    /**
     * @param email -- string email to find in db
     * @return -- User object
     * @throws UsernameNotFoundException -- throws UsernameNotFoundException
     */
    @Throws(UsernameNotFoundException::class)
    fun findByEmail(email: String): User =
        userRepository.findByEmail(email = email) ?: throw UsernameNotFoundException(messageSourceService.get(code = "user_not_found"))

    /**
     * @param user -- User object to persist to db
     * @return -- User object that is persisted to db
     */
    fun save(user: User): User = userRepository.save(user)

    fun createSpecificationForUser(q: String?): Specification<User> {
        return Specification { root: Root<User>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder ->
            val predicates: MutableList<Predicate> = ArrayList()
            if (!q.isNullOrEmpty()) {
                val predicateName: Predicate = criteriaBuilder.like(criteriaBuilder.lower(root["name"]), "%${q.lowercase()}%")
                val predicateEmail: Predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%${q.lowercase()}%")
                predicates.add(criteriaBuilder.or(predicateName, predicateEmail))
            }
            query.where(*predicates.toTypedArray()).distinct(true).restriction
        }
    }

    /**
     * @param email -- id
     * @return -- Spring User object
     */
    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(email: String): org.springframework.security.core.userdetails.User {
        val user: User = findByEmail(email = email)
        val authorities: List<GrantedAuthority> = user.roles.stream()
            .map { r: Role -> SimpleGrantedAuthority(RoleName.fromString(r = r.name.toString())!!.name) }
            .toList()
        return org.springframework.security.core.userdetails.User(user.email, user.password, authorities)
    }

    /**
     * @return -- User entity that is retrieved from db
     * @throws ServerException -- throws ServerException
     */
    @Throws(ServerException::class)
    fun loggedInUser(): User =
        (SecurityContextHolder.getContext().authentication.principal as org.springframework.security.core.userdetails.User).username
            .run { findByEmail(email = this) }

    /**
     * Activate user's email by token.
     * @param token String
     */
    fun activateEmail(token: String) {
        emailActivationTokenService.getUserByToken(token = token)
            .also { it.emailActivatedAt = Date() }
            .run { userRepository.save(this) }
            .run { emailActivationTokenService.deleteByUserId(userId = this.id!!) }
    }

    /**
     * PasswordReset user passwordReset from request.
     * @param email email
     */
    fun passwordReset(email: String) {
        val user: User = findByEmail(email = email)
        val byUserId: PasswordResetToken? = passwordResetTokenRepository.findByUserId(userId = user.id!!)
        if (byUserId != null) {
            messageSourceService.get(code = "password_reset_token_exist")
                .apply { log.error(this) }
                .run { throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                    statusCode = HttpStatus.CONFLICT, variables = arrayOf(this)) }
        }
        val token: String = 50.randomStringGenerator()
        PasswordResetToken(token = token, userId = user.id!!)
            .run { passwordResetTokenRepository.save(this) }
        emailService.sendResetPasswordEmail(user = user, token = token)
    }

    /**
     * Changes the user's password from request.
     * @param request ChangePasswordRequest.
     * @param token   String.
     */
    fun changePassword(request: ChangePasswordRequest, token: String) {
        val passwordResetToken: PasswordResetToken = passwordResetTokenRepository.findByToken(token = token)
             ?: messageSourceService.get(code = "password_reset_token_expired", params = arrayOf(token))
                 .apply { log.error(this) }
                 .run { throw ServerException(omaErrorMessageType = OmaErrorMessageType.NOT_FOUND,
                     statusCode = HttpStatus.NOT_FOUND, variables = arrayOf(this)) }

        var user: User = findByEmail(email = request.email)
        if (passwordResetToken.userId != user.id)
            messageSourceService.get(code = "invalid_token_for_mail")
                .apply { log.error(this) }
                .run { throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                    statusCode = HttpStatus.BAD_REQUEST, variables = arrayOf(this)) }

        if (request.password != request.passwordConfirmation)
            messageSourceService.get(code = "password_mismatch")
                .apply { log.error(this) }
                .run { throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                    statusCode = HttpStatus.BAD_REQUEST, variables = arrayOf(this)) }

        if (user.password.equals(request.password))
            messageSourceService.get(code = "new_password_must_be_different_from_old")
                .apply { log.error(this) }
                .run { throw ServerException(omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                    statusCode = HttpStatus.CONFLICT, variables = arrayOf(this)) }
        user.password = passwordEncoder.encode(request.password)
        user = userRepository.save(user)
        passwordResetTokenRepository.delete(passwordResetToken)
        emailService.sendChangePasswordSuccess(user)
    }
}
