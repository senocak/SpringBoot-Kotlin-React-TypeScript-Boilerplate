package com.github.senocak.auth.kafka.consumer

import com.github.senocak.auth.domain.dto.TpsInfo
import org.apache.kafka.clients.consumer.ConsumerRecord

interface IKafkaClient {
    fun init()
    fun destroy()
    fun processMessage(consumerRecord: ConsumerRecord<String, String>)
    val tpsInfo: TpsInfo
}
