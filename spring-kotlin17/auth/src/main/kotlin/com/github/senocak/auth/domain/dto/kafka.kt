package com.github.senocak.auth.domain.dto

import java.util.Date

class TpsInfo {
    var maxTps = 0
    var lastScheduledTime: Long = 0
    var currentProcessedTxnCount = 0
}

enum class KafkaAction(val messageId: String) {
    MAKE_BUCKET("make_bucket"),
    DELETE_BUCKET("delete_bucket")
}

data class BucketMessageTemplate(
    val action: KafkaAction,
    val value: String
) {
    var createdTime: Long = Date().time
}

class DLTDto(
    var topic: String,
    var data: String,
    var exception: String,
)