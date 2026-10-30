package io.nikhil.cqrsbankaccount.core.handlers

import io.nikhil.cqrsbankaccount.core.domain.AggregateRoot

interface EventSourcingHandler<T> {
    fun save(aggregate: AggregateRoot)
    fun getById(id: String): T
    fun republishEvents()
}
