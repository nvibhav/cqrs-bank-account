package io.nikhil.cqrsbankaccount.core.events

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "eventStore")
data class EventModel(
    @Id var id: String? = null,
    var timestamp: Date = Date(),
    var aggregateIdentifier: String = "",
    var aggregateType: String = "",
    var version: Int = 0,
    var eventType: String = "",
    var eventData: BaseEvent? = null,
)
