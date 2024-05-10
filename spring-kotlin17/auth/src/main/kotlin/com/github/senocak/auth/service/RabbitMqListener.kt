package com.github.senocak.auth.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.auth.domain.Mail
import com.github.senocak.auth.domain.dto.ServiceData
import com.github.senocak.auth.domain.dto.UserResponse
import com.github.senocak.auth.util.Action
import com.github.senocak.auth.util.logger
import org.slf4j.Logger
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import java.util.Locale

@Component
class RabbitMqListener(
    private val objectMapper: ObjectMapper,
    private val emailService: EmailService,
    private val htmlTemplateEngine: ITemplateEngine
) {
    private val log: Logger by logger()

    @RabbitListener(queues = ["\${app.rabbitmq.QUEUE}"])
    fun receiveMessage(data: String?) {
        log.info("Message received: $data")
        val serviceData: ServiceData
        try {
            serviceData = objectMapper.readValue(data, ServiceData::class.java)
        } catch (e: JsonProcessingException) {
            log.error("Error while parsing json: ${e.message}")
            return
        }
        when (serviceData.action) {
            Action.Login -> loginAction(message = serviceData.message!!)
            Action.Me -> generateMeMail(message = serviceData.message!!)
            Action.Logout -> logoutAction(message = serviceData.message!!)
            else -> log.warn("Unknown action: ${serviceData.message}")
        }
    }

    /**
     * Generate me mail.
     * @param message message
     */
    private fun generateMeMail(message: String) {
        val userResponse: UserResponse
        try {
            userResponse = objectMapper.readValue(message, UserResponse::class.java)
        } catch (e: JsonProcessingException) {
            log.error("Error while parsing json: ${e.message}")
            return
        }
        val ctx = Context(Locale("en"))
        ctx.setVariable("user", userResponse)
        val mail = Mail()
            .also {
                it.from = "configService.getEmailHash().getFrom()"
                it.to = "senocakanil@gmail.com"
                it.content = htmlTemplateEngine.process("welcome", ctx)
                it.subject = "New Login Attempt"
            }
        emailService.sendMail(mail = mail)
    }

    /**
     * Login action.
     * @param message message
     */
    private fun loginAction(message: String): Unit = log.info("Login action: {}", message)

    /**
     * Logout action.
     * @param message message
     */
    private fun logoutAction(message: String): Unit = log.info("Logout action: {}", message)
}
