package com.github.senocak.auth.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.senocak.auth.domain.dto.DLTDto
import com.github.senocak.auth.kafka.producer.DLTKafkaProducer
import com.github.senocak.auth.util.logger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.header.Header
import org.slf4j.Logger
import java.nio.charset.Charset
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class KafkaMsgConsumer(
    private val consumer: KafkaConsumer<String, String>,
    private val threadNumber: Int,
    private val owner: IKafkaClient,
    private val applicationName: String,
    private val dlt: DLTKafkaProducer
) : Runnable {
    private val log: Logger by logger()
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    // the variable provides stop consuming message by consumer
    @Volatile
    private var running = true

    override fun run() {
        log.info("KafkaConsumer Thread-$threadNumber is running for ${owner.javaClass.simpleName}")

        /*
         * get message from consumer iterator at the Next moment kafka wait during
         * 100ms(auto.commit.interval.ms) then commit to zookeeper the offset of
         * messages already fetched by the consumer
         */
        while (running) {
            try {
                val consumerRecords: ConsumerRecords<String, String> = consumer.poll(Duration.ofMillis(SECONDS.toLong())) // new in Kafka 2.0
                consumerRecords.forEach(
                    Consumer<ConsumerRecord<String, String>> { consumerRecord: ConsumerRecord<String, String> ->
                        val fromHeader: Header? = consumerRecord.headers()
                            .find { header: Header -> ("from" == header.key()) && (applicationName == String(bytes = consumerRecord.headers().first().value(), charset = Charset.defaultCharset())) }
                        if (fromHeader != null && consumerRecord.topic() != "dead-letter-queue") {
                            log.warn("Message consumed by produced service so ignoring...")
                            return@Consumer
                        }
                        log.info("""
                        consumerRecord.headers. ${consumerRecord.headers()}
                        key: ${consumerRecord.key()}
                        value: ${consumerRecord.value()}
                        partition: ${consumerRecord.partition()}
                        topic: ${consumerRecord.topic()}
                        offset: ${consumerRecord.offset()}
                        threadNumber:$threadNumber""")
                        try {
                            owner.processMessage(consumerRecord = consumerRecord)
                        } catch (e: Exception) {
                            try {
                                dlt.produce(msg = objectMapper.writeValueAsString(DLTDto(topic = consumerRecord.topic(),
                                    data = consumerRecord.value(), exception = "${e.message}")))
                                log.info("Message sent to DLQ, key: ${consumerRecord.key()}, value: ${consumerRecord.value()}")
                            } catch (e: Exception) {
                                log.error("Failed to send message to DLQ, key: ${consumerRecord.key()}, value: ${consumerRecord.value()}")
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                log.error("Consumer Records could not fetched: Ex: ${e.message}")
            } finally {
                if (owner.tpsInfo.maxTps > 0) {
                    checkCurrentTps()
                }
            }
        }
    }

    @Synchronized
    fun checkCurrentTps() {
        owner.tpsInfo.currentProcessedTxnCount++
        // for each 1000 ms, clean counter and reset last scheduled time.
        // in last 1000 ms, if consumed message count less than maxTps, no need to sleep.
        // run, run, run...
        if (System.currentTimeMillis() - owner.tpsInfo.lastScheduledTime > SECONDS) {
            owner.tpsInfo.lastScheduledTime = System.currentTimeMillis()
            owner.tpsInfo.currentProcessedTxnCount = 0
        }

        // in last 1000 ms, if consumed message count greater than maxTps,
        // it's time to sleep.
        // other threads will wait last slept thread because of synchronized method.
        if (owner.tpsInfo.currentProcessedTxnCount >= owner.tpsInfo.maxTps) {
            try {
                // elapsed time after last sleep time
                val elapsed: Long = System.currentTimeMillis() - owner.tpsInfo.lastScheduledTime
                // sleep delta time to complete 1 sec.
                TimeUnit.MILLISECONDS.sleep(SECONDS - elapsed)
            } catch (ex: InterruptedException) {
                log.error("Interrupted Exception was occurred while checking currentTps in KafkaMsgConsumer:${ex.message}")
                // Restore interrupted state...
                Thread.currentThread().interrupt()
            } finally {
                owner.tpsInfo.currentProcessedTxnCount = 0
                owner.tpsInfo.lastScheduledTime = System.currentTimeMillis()
            }
        }
    }

    fun stop() {
        log.info("Shutting down of KafkaMsgConsumer for owner: ${owner.javaClass.simpleName} and threadNumber:$threadNumber")
        try {
            running = false
            // Wait for running flag is to be taken effective for next 1 sec period.
            TimeUnit.MILLISECONDS.sleep(SECONDS.toLong())
            consumer.close()
        } catch (ex: Exception) {
            log.error("Exception while closing KafkaMsgConsumer for owner:${owner.javaClass.simpleName} and threadNumber:$threadNumber")
        }
        log.info("Shutting downed of KafkaMsgConsumer was finished for owner:${owner.javaClass.simpleName} and threadNumber: $threadNumber")
    }

    companion object {
        private const val SECONDS = 1_000
    }
}
