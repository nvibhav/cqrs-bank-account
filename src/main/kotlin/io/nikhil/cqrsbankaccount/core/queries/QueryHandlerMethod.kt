package io.nikhil.cqrsbankaccount.core.queries

import io.nikhil.cqrsbankaccount.core.domain.BaseEntity

fun interface QueryHandlerMethod<T: BaseQuery> {
    fun handle(query: T): List<BaseEntity>
}
