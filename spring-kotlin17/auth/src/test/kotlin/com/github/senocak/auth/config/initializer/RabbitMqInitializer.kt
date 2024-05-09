package com.github.senocak.auth.config.initializer

import com.github.senocak.auth.TestConstants
import com.rabbitmq.client.ConnectionFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.springframework.boot.test.util.TestPropertyValues
import com.rabbitmq.client.Channel

@TestConfiguration
class RabbitMqInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        val host: String = CONTAINER.host
        val port: Int = CONTAINER.getMappedPort(RABBIT_MQ_PORT)
        TestPropertyValues.of(
                "spring.rabbitmq.host=$host",
                "spring.rabbitmq.port=$port",
                "spring.rabbitmq.user=guest",
                "spring.rabbitmq.password=guest"
            )
            .applyTo(configurableApplicationContext.environment)

        val factory = ConnectionFactory()
            .also {
                it.host = host
                it.port = port
                it.username = "guest"
                it.password = "guest"
            }
        try {
            factory.newConnection().use { connection -> rabbitmq = connection.createChannel() }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        if (rabbitmq == null)
            throw RuntimeException("RabbitMQ is not initialized!")
    }

    companion object {
        private const val RABBIT_MQ_PORT = 5672
        var rabbitmq: Channel? = null

        @Container private var CONTAINER: GenericContainer<*> = GenericContainer("rabbitmq:3.6-management-alpine")
            .withExposedPorts(RABBIT_MQ_PORT)
            .withEnv("RABBITMQ_IO_THREAD_POOL_SIZE", "4")
            .withStartupTimeout(TestConstants.CONTAINER_WAIT_TIMEOUT)
            .waitingFor(Wait.forListeningPort())

        init {
            CONTAINER.start()
        }
    }
}