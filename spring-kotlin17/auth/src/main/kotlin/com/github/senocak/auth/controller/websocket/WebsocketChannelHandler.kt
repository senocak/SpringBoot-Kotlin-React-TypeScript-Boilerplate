package com.github.senocak.auth.controller.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.auth.domain.dto.WebsocketIdentifier
import com.github.senocak.auth.domain.dto.WsRequestBody
import com.github.senocak.auth.exception.ServerException
import com.github.senocak.auth.security.JwtTokenProvider
import com.github.senocak.auth.service.WebSocketCacheService
import com.github.senocak.auth.util.OmaErrorMessageType
import com.github.senocak.auth.util.getQueryParams
import com.github.senocak.auth.util.logger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import org.slf4j.Logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler

@Controller
class WebsocketChannelHandler(
    private val webSocketCacheService: WebSocketCacheService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val objectMapper: ObjectMapper
): AbstractWebSocketHandler() {
    private val log: Logger by logger()
    private val lock = ReentrantLock(true)

    /**
     * A method that is called when a new WebSocket session is created.
     * @param session The new WebSocket session.
     */
    override fun afterConnectionEstablished(session: WebSocketSession): Unit =
        lock.withLock {
            runCatching {
                log.info("Websocket connection established. Path: ${session.uri!!.path}")
                if (session.uri == null)
                    log.error("Unable to retrieve the websocket session; serious error!").also { return }
                val (email: String, token: String) = getUserEmailAndAccessTokenFromQueryParams(query = session.uri!!.query)
                WebsocketIdentifier(user = email, token = token, session = session)
                    .also { log.info("Websocket session established: $it") }
                    .run { webSocketCacheService.put(data = this) }
            }.onFailure {
                log.error("A serious error has occurred with websocket post-connection handling. Ex: ${it.message}")
            }
        }

    /**
     * A method that is called when a WebSocket session is closed.
     * @param session The WebSocket session that is closed.
     * @param status The status of the close.
     */
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus): Unit =
        lock.withLock {
            log.info("Websocket connection closed. Path: ${session.uri!!.path}")
            runCatching {
                if (session.uri == null)
                    log.error("Unable to retrieve the websocket session; serious error!").also { return }
                val (email: String, _: String) = getUserEmailAndAccessTokenFromQueryParams(query = session.uri!!.query)
                webSocketCacheService.deleteSession(key = email)
                    .also { log.info("Websocket for $email has been closed") }
            }.onFailure {
                log.error("Error occurred while closing websocket channel:${it.message}")
            }
        }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        when (message) {
            is PingMessage -> log.info("PingMessage: $message")
            is PongMessage -> log.info("PongMessage: $message")
            is BinaryMessage -> log.info("BinaryMessage: $message")
            is TextMessage -> {
                val body: WsRequestBody = objectMapper.readValue(message.payload, WsRequestBody::class.java)
                log.info("TextMessage: $body")
                try {
                    val requestBody: WsRequestBody = objectMapper.readValue(message.payload, WsRequestBody::class.java)
                    val (email: String, _: String) = getUserEmailAndAccessTokenFromQueryParams(query = session.uri!!.query)
                    requestBody.from = email
                    webSocketCacheService.sendPrivateMessage(requestBody = requestBody)
                    // TODO: save it to db
                    log.info("Websocket message sent: ${message.payload}")
                } catch (ex: Exception) {
                    log.error("Unable to parse request body; Exception: ${ex.message}")
                }
            }
            else -> session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Not supported"))
                .also { log.error("Not supported. ${message.javaClass}") }
        }
    }

    private fun getUserEmailAndAccessTokenFromQueryParams(query: String): Pair<String, String>  {
        val queryParams: Map<String, String> = query.getQueryParams() ?: throw Exception("QueryParams can not be empty")
        val accessToken: String = queryParams["access_token"] ?: throw Exception("Auth can not be empty")
        return Pair(first = jwtTokenProvider.getUserEmailFromJWT(token = accessToken), second = accessToken)
    }

    private fun getUserIdAndAccessTokenFromHeader(headers: HttpHeaders): Pair<String, String> =
        headers["Authorization"]
            .run {
                return when {
                    !this.isNullOrEmpty() -> {
                        var first: String = this.first()
                        if (first.startsWith(prefix = "Bearer "))
                            first = first.substring(startIndex = 7)
                        Pair(first = jwtTokenProvider.getUserEmailFromJWT(token = first), second = first)
                    }
                    else -> throw ServerException(omaErrorMessageType = OmaErrorMessageType.GENERIC_SERVICE_ERROR,
                        variables = arrayOf("token is invalid"), statusCode = HttpStatus.INTERNAL_SERVER_ERROR)
                }
            }
}