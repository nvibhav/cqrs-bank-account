package io.nikhil.cqrsbankaccount.core.domain

abstract class AggregateRoot {
    protected lateinit var id: String
    var version: Int = -1

    private val changes = mutableListOf<BaseEvent>()
}
