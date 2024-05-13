package com.github.senocak.auth.kafka.producer

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Service

// @Scope(value = "prototype")
@Service
class BucketCreateRequestKafkaProducer : AbsKafkaProducer<String, String>(), SmartLifecycle {
    var running: Boolean = false

    @Value("\${app.kafka.producer.topic.bucket-create}")
    override lateinit var topic: String

    @Value("\${app.kafka.bootstrap-servers}")
    override lateinit var brokerList: String

    override fun start(): Unit = super.init().also { running = true }
    override fun stop(): Unit = super.destroy().also { running = false }
    override fun isRunning(): Boolean = running
}
