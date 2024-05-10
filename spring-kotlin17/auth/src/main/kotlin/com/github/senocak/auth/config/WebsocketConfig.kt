package com.github.senocak.auth.config

import com.github.senocak.auth.controller.websocket.HttpHandshakeInterceptor
import com.github.senocak.auth.controller.websocket.WebsocketChannelHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebsocketConfig(
    private val websocketChannelHandler: WebsocketChannelHandler,
    private val httpHandshakeInterceptor: HttpHandshakeInterceptor
) : WebSocketConfigurer {
    /**
     * Register websocket handlers.
     * @param registry WebSocketHandlerRegistry
     */
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(websocketChannelHandler, "/ws")
            .addInterceptors(httpHandshakeInterceptor)
            .setAllowedOrigins("*")
    }
}
