package com.github.senocak.auth.config.initializer

import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.senocak.auth.TestConstants
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container

@TestConfiguration
class PostgresqlInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of(
            "spring.datasource.url=" + CONTAINER.jdbcUrl,
            "spring.datasource.username=" + CONTAINER.username,
            "spring.datasource.password=" + CONTAINER.password
        ).applyTo(configurableApplicationContext.environment)
    }

    companion object {
        @Container private var CONTAINER: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:14")
            //.withExposedPorts(3306)
            .withDatabaseName("spring")
            .withUsername("postgres")
            .withPassword("secret")
            .withInitScript("db.sql")
            .withStartupTimeout(TestConstants.CONTAINER_WAIT_TIMEOUT)
            .withCreateContainerCmdModifier { cmd: CreateContainerCmd -> cmd.withName("SQL_CONTAINER") }

        init {
            CONTAINER.start()
        }
    }
}