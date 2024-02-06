package com.github.senocak.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.auth.domain.dto.WebsocketIdentifier
import com.github.senocak.auth.domain.dto.WsRequestBody
import com.github.senocak.auth.util.logger
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import java.io.IOException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class WebSocketCacheService(
    private val objectMapper: ObjectMapper
) {
    private val log: Logger by logger()
    private val userSessionCache: MutableMap<String, WebsocketIdentifier> = ConcurrentHashMap<String, WebsocketIdentifier>()

    /**
     * Get all websocket session cache.
     * @return map of websocket session cache.
     */
    val allWebSocketSession: Map<String, WebsocketIdentifier> get() = userSessionCache

    /**
     * Add websocket session cache.
     * @param data websocket session cache.
     */
    fun put(data: WebsocketIdentifier) {
        userSessionCache[data.user] = data
        broadCastMessage(message = data.user, type = "login")
        broadCastAllUserList(to = data.user)
    }

    /**
     * Get or default websocket session cache.
     * @param key key of websocket session cache.
     * @return websocket session cache.
     */
    fun getOrDefault(key: String): WebsocketIdentifier? =
        userSessionCache.getOrDefault(key = key, defaultValue = null)

    /**
     * Remove websocket session cache.
     * @param key key of websocket session cache.
     */
    fun deleteSession(key: String) {
        val websocketIdentifier: WebsocketIdentifier? = getOrDefault(key = key)
        if (websocketIdentifier?.session == null) {
            log.error("Unable to remove the websocket session; serious error!")
            return
        }
        userSessionCache.remove(key = key)
        broadCastAllUserList(to = websocketIdentifier.user)
        broadCastMessage(message = websocketIdentifier.user, type = "logout")
    }

    /**
     * Broadcast message to all websocket session cache.
     * @param message message to broadcast.
     */
    private fun broadCastMessage(message: String, type: String) {
        val wsRequestBody = WsRequestBody()
            .also {
                it.content = message
                it.date = Instant.now().toEpochMilli()
                it.type = type
            }
        allWebSocketSession.forEach { entry ->
            try {
                entry.value.session!!.sendMessage(TextMessage(objectMapper.writeValueAsString(wsRequestBody)))
            } catch (e: Exception) {
                log.error("Exception while broadcasting: ${e.message}")
            }
        }
    }

    /**
     * Broadcast message to specific websocket session cache.
     * @param requestBody message to send.
     */
    fun sendPrivateMessage(requestBody: WsRequestBody) {
        val userTo: WebsocketIdentifier? = getOrDefault(key = requestBody.to!!)
        if (userTo?.session == null) {
            log.error("User or Session not found in cache for user: ${requestBody.to}, returning...")
            return
        }
        requestBody.type = "private"
        requestBody.date = Instant.now().toEpochMilli()
        try {
            userTo.session!!.sendMessage(TextMessage(objectMapper.writeValueAsString(requestBody)))
        } catch (e: IOException) {
            log.error("Exception while sending message: ${ExceptionUtils.getMessage(e)}")
        }
    }

    /**
     * Broadcast message to specific websocket session cache.
     * @param from from user.
     * @param payload message to send.
     */
    fun sendMessage(from: String?, to: String, type: String?, payload: String?) {
        val userTo: WebsocketIdentifier? = getOrDefault(key = to)
        if (userTo?.session == null) {
            log.error("User or Session not found in cache for user: $to, returning...")
            return
        }
        val requestBody: WsRequestBody = WsRequestBody()
            .also {
                it.from = from
                it.to = to
                it.date = Instant.now().toEpochMilli()
                it.content = payload
                it.type = type
            }
        try {
            userTo.session!!.sendMessage(TextMessage(objectMapper.writeValueAsString(requestBody)))
        } catch (e: IOException) {
            log.error("Exception while sending message: ${ExceptionUtils.getMessage(e)}")
        }
    }

    /**
     * Broadcast message to all websocket session cache.
     * @param to user to broadcast.
     */
    private fun broadCastAllUserList(to: String): Unit =
        sendMessage(from = null, to = to, type = "online", payload = StringUtils.join(userSessionCache.keys,','))
}