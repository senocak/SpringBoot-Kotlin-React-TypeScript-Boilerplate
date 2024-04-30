package com.github.senocak.auth.service

import com.github.senocak.auth.util.logger
import io.debezium.config.Configuration
import io.debezium.data.Envelope.FieldName.AFTER
import io.debezium.data.Envelope.FieldName.BEFORE
import io.debezium.data.Envelope.FieldName.OPERATION
import io.debezium.data.Envelope.Operation
import io.debezium.embedded.Connect
import io.debezium.engine.DebeziumEngine
import io.debezium.engine.RecordChangeEvent
import io.debezium.engine.format.ChangeEventFormat
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.slf4j.Logger
import org.springframework.stereotype.Service

@Service
class DebeziumListener(configuration: Configuration) {
    private val log: Logger by logger()
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private lateinit var debeziumEngine: DebeziumEngine<RecordChangeEvent<SourceRecord>>

    init {
        this.debeziumEngine =
            DebeziumEngine.create(ChangeEventFormat.of(Connect::class.java))
                .using(configuration.asProperties())
                .notifying {
                    sourceRecordRecordChangeEvent: RecordChangeEvent<SourceRecord> ->
                    this.handleChangeEvent(sourceRecordRecordChangeEvent)
                }
                .build()
    }

    private fun handleChangeEvent(sourceRecordRecordChangeEvent: RecordChangeEvent<SourceRecord>) {
        val sourceRecord = sourceRecordRecordChangeEvent.record()
        val sourceRecordKey = sourceRecord.key() as Struct
        val sourceRecordValue = sourceRecord.value() as Struct
        val operation = Operation.forCode("${sourceRecordValue.get(OPERATION)}")
        val before = sourceRecordValue.get(BEFORE)
        val after = sourceRecordValue.get(AFTER)
        log.info("sourceRecordKey:$sourceRecordKey, operation:$operation, before: $before, after: $after")
    }

    @PostConstruct
    private fun start() {
        executor.execute(debeziumEngine)
    }

    @PreDestroy
    @Throws(IOException::class)
    private fun stop() = run { debeziumEngine.close() }
}
