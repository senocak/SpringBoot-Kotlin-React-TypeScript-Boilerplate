package com.github.senocak.auth.service

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.senocak.auth.domain.Mail
import com.github.senocak.auth.domain.dto.RoleResponse
import com.github.senocak.auth.domain.dto.ServiceData
import com.github.senocak.auth.domain.dto.UserResponse
import com.github.senocak.auth.util.Action
import com.github.senocak.auth.util.RoleName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.thymeleaf.ITemplateEngine

@Tag("unit")
@DisplayName("Unit Tests for RabbitMqListener")
class RabbitMqListenerTest {
    private lateinit var rabbitMqListener: RabbitMqListener
    private val emailService: EmailService = Mockito.mock(EmailService::class.java)
    private val htmlTemplateEngine: ITemplateEngine = mock<ITemplateEngine>()
    private val objectMapper = jacksonObjectMapper().registerModule(KotlinModule.Builder().build())

    @BeforeEach
    fun init() {
        rabbitMqListener = RabbitMqListener(
            objectMapper = objectMapper,
            emailService = emailService,
            htmlTemplateEngine = htmlTemplateEngine
        )
    }

    @Test
    fun givenData_whenReceiveMessageWithActionLogin_thenAssertResult() {
        // Given
        val sd = ServiceData(action = Action.Login, message = "message")
        val sdString = objectMapper.writeValueAsString(sd)
        // When
        assertDoesNotThrow {
            rabbitMqListener.receiveMessage(data = sdString)
        }
    }

    @Test
    fun givenData_whenReceiveMessageWithActionLogout_thenAssertResult() {
        // Given
        val sd = ServiceData(action = Action.Logout, message = "message")
        val sdString = objectMapper.writeValueAsString(sd)
        // When
        assertDoesNotThrow {
            rabbitMqListener.receiveMessage(data = sdString)
        }
    }

    @Test
    fun givenData_whenReceiveMessageWithActionMe_thenAssertResult() {
        // Given
        val roles = RoleResponse().also { it.name = RoleName.ROLE_USER }
        val message = UserResponse(name = "name1", email = "email1", roles = arrayListOf(roles))
        val sd = ServiceData(action = Action.Me, message = objectMapper.writeValueAsString(message))
        val sdString = objectMapper.writeValueAsString(sd)
        // When
        assertDoesNotThrow {
            rabbitMqListener.receiveMessage(data = sdString)
        }
        // Then
        val mail = Mail()
            .also {
                it.from = "configService.getEmailHash().getFrom()"
                it.to = "senocakanil@gmail.com"
                it.content = null
                it.subject = "New Login Attempt"
            }
        verify(emailService).sendMail(mail = eq(value = mail))
    }
}
