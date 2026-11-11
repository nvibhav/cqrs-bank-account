package io.nikhil.cqrsbankaccount.core.events

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("commandReceipts")
data class CommandReceipt(
    @Id val commandId: String,
    val aggregateIdentifier: String,
    val fingerprint: String,
)
