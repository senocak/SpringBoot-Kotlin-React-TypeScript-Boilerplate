package com.github.senocak.auth.controller.websocket

import com.github.senocak.auth.domain.dto.WebsocketIdentifier
import com.github.senocak.auth.security.JwtTokenProvider
import com.github.senocak.auth.service.WebSocketCacheService
import com.github.senocak.auth.util.getQueryParams
import com.github.senocak.auth.util.logger
import org.slf4j.Logger
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor
import java.net.URI
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Component
class HttpHandshakeInterceptor(
    private val webSocketCacheService: WebSocketCacheService,
    private val jwtTokenProvider: JwtTokenProvider
) : HttpSessionHandshakeInterceptor() {
    private val log: Logger by logger()
    private val lock = ReentrantLock(true)

    /**
     * Check if session is created.
     * @return true if session is created.
     */
    override fun isCreateSession(): Boolean = super.isCreateSession().also { log.debug("isCreateSession : $it") }

    /**
     * A method beforeHandshake is called before the WebSocket handshake is completed.
     * @param request the current request
     * @param response the current response
     * @param wsHandler the target WebSocket handler
     * @param attributes attributes from the HTTP handshake to associate with the WebSocket
     * session; the provided attributes are copied, the original map is not used.
     * @return true if the WebSocket handshake should continue, false if the WebSocket
     */
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: Map<String, Any>
    ): Boolean {
        lock.withLock {
            val requestUri: URI = request.uri
            log.info("[HttpHandshakeInterceptor:beforeHandshake] requestUri: $requestUri, requestPath: ${requestUri.path}, header: ${request.headers}")

            runCatching {
                val (userId: String, _: String) = getAccessTokenFromQueryParams(query = requestUri.query)
                val allWebSocketSession: Map<String, WebsocketIdentifier> = webSocketCacheService.allWebSocketSession
                if (allWebSocketSession.containsKey(key = userId)) {
                    return false.also { log.warn("User already exists in the websocket session cache; rejecting websocket connection attempt!") }
                }
            }.onFailure {
                log.warn("Rejecting websocket connection attempt! ${it.message}")
                    .run { return false }
            }
            return true
        }
    }

    /**
     * A method afterHandshake is called after the WebSocketHandler has been created.
     * @param request the current request
     * @param response the current response
     * @param wsHandler the target WebSocket handler
     * @param ex an exception raised during the handshake, or `null` if none
     */
    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        ex: Exception?
    ) {
        log.info("[HttpHandshakeInterceptor:afterHandshake] request: $request, response: $response, wsHandler: $wsHandler, ex: $ex")
    }

    private fun getAccessTokenFromQueryParams(query: String): Pair<String, String> {
        val queryParams: Map<String, String> = query.getQueryParams() ?: throw Exception("QueryParams can not be empty")
        val accessToken: String = queryParams["access_token"] ?: throw Exception("Auth can not be empty")
        return Pair(first = jwtTokenProvider.getUserEmailFromJWT(token = accessToken), second = accessToken)
    }
}
