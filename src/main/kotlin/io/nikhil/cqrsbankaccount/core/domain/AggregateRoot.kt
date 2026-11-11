package io.nikhil.cqrsbankaccount.core.domain

import io.nikhil.cqrsbankaccount.core.events.BaseEvent

abstract class AggregateRoot {
    protected lateinit var id: String
    var version: Int = -1

    private val changes = mutableListOf<BaseEvent>()
}
