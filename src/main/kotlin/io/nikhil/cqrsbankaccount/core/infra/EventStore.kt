package io.nikhil.cqrsbankaccount.core.infra

import io.nikhil.cqrsbankaccount.core.events.BaseEvent

interface EventStore {
    fun saveEvents(aggregateId: String, events: Iterable<BaseEvent>, expectedVersion: Int)
    fun getEvents(aggregateId: String): List<BaseEvent>
    fun getAggregateIds(): List<String>
}
