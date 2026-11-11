package io.nikhil.cqrsbankaccount.core.infra

import io.nikhil.cqrsbankaccount.core.events.BaseEvent

interface EventStore {
    fun saveEvents(
        aggregateId: String,
        events: Iterable<BaseEvent>,
        expectedVersion: Int,
        commandId: String,
        commandFingerprint: String,
    ): String
    fun findCommandResult(commandId: String, commandFingerprint: String): String?
    fun getEvents(aggregateId: String): List<BaseEvent>
    fun getEventsAfter(aggregateId: String, version: Int): List<BaseEvent>
    fun getAggregateIds(): List<String>
}
