package com.github.senocak.auth.util

import ch.qos.logback.classic.Level
import com.github.senocak.auth.domain.Role
import com.github.senocak.auth.domain.User
import com.github.senocak.auth.domain.dto.RoleResponse
import com.github.senocak.auth.domain.dto.UserResponse
import java.text.Normalizer
import java.util.Date
import java.util.regex.Pattern
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils

/**
 * @return -- UserResponse object
 */
fun User.convertEntityToDto(): UserResponse =
    UserResponse(
        name = this.name!!,
        email = this.email!!,
        roles = this.roles.stream().map { r: Role -> r.convertEntityToDto() }.toList(),
        emailActivatedAt = this.emailActivatedAt?.time
    )

/**
 * @return -- RoleResponse object
 */
fun Role.convertEntityToDto(): RoleResponse {
    val roleResponse = RoleResponse()
    roleResponse.name = this.name
    return roleResponse
}

/**
 * @param date -- Date object to convert to long timestamp
 * @return -- converted timestamp object that is long type
 */
private fun convertDateToLong(date: Date): Long = date.time / 1000

/**
 * @param input -- string variable to make it sluggable
 * @return -- sluggable string variable
 */
fun String.toSlug(input: String): String {
    val nonLatin: Pattern = Pattern.compile("[^\\w-]")
    val whiteSpace: Pattern = Pattern.compile("[\\s]")
    val noWhiteSpace: String = whiteSpace.matcher(input).replaceAll("-")
    val normalized: String = Normalizer.normalize(noWhiteSpace, Normalizer.Form.NFD)
    return nonLatin.matcher(normalized).replaceAll("")
}

fun <R : Any> R.logger(): Lazy<Logger> = lazy {
    LoggerFactory.getLogger((if (javaClass.kotlin.isCompanion) javaClass.enclosingClass else javaClass).name)
}

fun Logger.changeLevel(loglevel: String) {
    val logger: ch.qos.logback.classic.Logger = this as ch.qos.logback.classic.Logger
    logger.level = Level.toLevel(loglevel)
    this.debug("Logging level: ${this.name}")
    this.info("Logging level: ${this.name}")
    this.warn("Logging level: ${this.name}")
    this.error("Logging level: ${this.name}")
}

//fun getLogger(): ch.qos.logback.classic.Logger =
//    LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger

/**
 * Split a string into two parts, separated by a delimiter.
 * @param delimiter The delimiter string
 * @return The array of two strings.
 */
fun String.split(delimiter: String): Array<String>? = StringUtils.split(this, delimiter)