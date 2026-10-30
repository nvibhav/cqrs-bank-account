package io.nikhil.cqrsbankaccount.core.producers

import io.nikhil.cqrsbankaccount.core.events.BaseEvent

interface EventProducer {
    fun produce(topic: String, event: BaseEvent)
}
