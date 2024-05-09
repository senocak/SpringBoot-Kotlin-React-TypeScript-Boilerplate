package com.github.senocak.auth.config

import com.github.senocak.auth.service.RabbitMqListener
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig(
    @Value("\${app.rabbitmq.HOST}") private val rabbitmqHost: String,
    @Value("\${app.rabbitmq.PORT}") private val rabbitmqPort: Int,
    @Value("\${app.rabbitmq.USER}") private val rabbitmqUser: String,
    @Value("\${app.rabbitmq.SECRET}") private val rabbitmqSecret: String,
    @Value("\${app.rabbitmq.EXCHANGE}") private val exchange: String,
    @Value("\${app.rabbitmq.QUEUE}") private val queue: String,
    @Value("\${app.rabbitmq.ROUTING_KEY}") private val routingKey: String
){

    /**
     * @return the queue
     */
    @Bean
    fun queue(): Queue = Queue(queue, false)

    /**
     * @return the exchange
     */
    @Bean
    fun exchange(): TopicExchange = TopicExchange(exchange)

    /**
     * @param queue the queue to set
     * @param exchange the exchange to set
     * @return the binding
     */
    @Bean
    fun binding(queue: Queue, exchange: TopicExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).with(routingKey)

    /**
     * @param connectionFactory the connectionFactory to set
     * @param listenerAdapter the listenerAdapter to set
     * @return the container
     */
    @Bean
    fun container(connectionFactory: ConnectionFactory, listenerAdapter: MessageListenerAdapter): SimpleMessageListenerContainer =
        SimpleMessageListenerContainer()
            .also {
                it.connectionFactory = connectionFactory
                it.setQueueNames(queue)
                it.setMessageListener(listenerAdapter)
            }

    /**
     * @param receiver the receiver to set
     * @return the listenerAdapter
     */
    @Bean
    fun listenerAdapter(receiver: RabbitMqListener): MessageListenerAdapter =
        MessageListenerAdapter(receiver, "receiveMessage")

    /**
     * @return the connectionFactory
     */
    @Bean
    fun connectionFactory(): ConnectionFactory =
        CachingConnectionFactory()
            .also {
                it.setHost(rabbitmqHost)
                it.port = rabbitmqPort
                it.username = rabbitmqUser
                it.setPassword(rabbitmqSecret)
            }
}