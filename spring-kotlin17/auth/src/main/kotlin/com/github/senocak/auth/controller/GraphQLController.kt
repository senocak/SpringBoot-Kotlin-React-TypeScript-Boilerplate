package com.github.senocak.auth.controller

import com.github.senocak.auth.util.changeLevel
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class GraphQLController {
    private val log = LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger

    @QueryMapping
    fun getLogLevel(): String = log.level.levelStr

    @MutationMapping
    fun changeLogLevel(@Argument loglevel: String): String = run {
        log.changeLevel(loglevel = loglevel)
        getLogLevel()
    }
}
