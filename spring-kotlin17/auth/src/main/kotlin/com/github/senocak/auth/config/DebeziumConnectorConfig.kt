package com.github.senocak.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import io.debezium.config.Configuration as DebeziumConfiguration

@Configuration
class DebeziumConnectorConfig(
    private val dataSourceConfig: DataSourceConfig
) {
    @Bean
    fun debeziumConfiguration(): DebeziumConfiguration = run {
        val matchResult = "jdbc:postgresql://([^:/]+):(\\d+)/([^?]+)".toRegex().find(input = "${dataSourceConfig.url}")
        DebeziumConfiguration.create()
            .with("name", "customer_postgres_connector")
            .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
            // .with("tasks.max", "1")
            .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
            .with("offset.storage.file.filename", File.createTempFile("offsets_", ".dat").absolutePath)
            .with("offset.flush.interval.ms", "60000")
            .with("database.hostname", matchResult!!.groupValues[1])
            .with("database.port", matchResult.groupValues[2])
            .with("database.user", dataSourceConfig.username)
            .with("database.password", dataSourceConfig.password)
            .with("database.dbname", matchResult.groupValues[3])
            .with("database.server.id", "10001")
            .with("database.server.name", "postgres-${matchResult.groupValues[3]}")
            .with("topic.prefix", "slave-postgresql-${matchResult.groupValues[3]}")
            .with("database.history", "io.debezium.relational.history.MemoryDatabaseHistory")
            .with("table.include.list", "public.users")
            // .with("column.include.list", "public.users.email,public.customer.name")
            // .with("column.include.list", "storeDB.customer.*,storeDB.product.(id|price)")
            .with("publication.autocreate.mode", "all_tables")
            .with("plugin.name", "pgoutput")
            .with("slot.name", "dbz_customerdb_listener")
            .with("schema.history.internal.kafka.bootstrap.servers", "localhost:29092")
            .with("schema.history.internal.kafka.topic", "schema-changes.inventory")
            .build()
    }
}
