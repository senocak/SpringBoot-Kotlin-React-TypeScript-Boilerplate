package com.github.senocak.auth.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.socket.WebSocketSession

data class WebsocketIdentifier(
    @Schema(example = "user", description = "user", required = true, name = "username", type = "String")
    var user: String,

    @Schema(description = "token", name = "token", type = "String", example = "token", required = true)
    var token: String? = null,

    @Schema(description = "session", name = "session", type = "String", example = "session", required = true)
    var session: WebSocketSession? = null
) : BaseDto()

data class WsRequestBody(
    var from: String? = null,
    var to: String? = null,
    var content: String? = null,
    var type: String? = null,
    var date: Long? = null
) : BaseDto()
