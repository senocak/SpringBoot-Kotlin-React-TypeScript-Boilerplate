package com.github.senocak.auth

import com.github.senocak.auth.util.changeLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener

@SpringBootApplication
class SpringKotlinApplication

val log: Logger = LoggerFactory.getLogger("main")
fun main(args: Array<String>) {
    // runApplication<SpringKotlinApplication>(*args)
    SpringApplicationBuilder(SpringKotlinApplication::class.java)
        .bannerMode(Banner.Mode.CONSOLE)
        .logStartupInfo(true)
        .listeners(
            ApplicationListener {
                    event: ApplicationEvent ->
                log.info("######## ApplicationEvent> ${event.javaClass.canonicalName}")
            }
        )
        .build()
        .run(*args)
    log.changeLevel(loglevel = Level.INFO.toString())
}
