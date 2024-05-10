package com.github.senocak.auth.domain

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.mail")
class MailConfigurationProperties {
    lateinit var host: String
    lateinit var port: String
    lateinit var protocol: String
    lateinit var username: String
    lateinit var password: String
}
data class Mail(
    var from: String? = null,
    var to: String? = null,
    var cc: String? = null,
    var bcc: String? = null,
    var subject: String? = null,
    var content: String? = null,
    var contentType: String = "text/html"
)
