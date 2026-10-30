package io.nikhil.cqrsbankaccount.core.infra

import io.nikhil.cqrsbankaccount.core.domain.BaseEntity
import io.nikhil.cqrsbankaccount.core.queries.BaseQuery
import io.nikhil.cqrsbankaccount.core.queries.QueryHandlerMethod

interface QueryDispatcher {
    fun <T : BaseQuery> registerHandler(type: Class<T>, handler: QueryHandlerMethod<T>)
    fun <U : BaseEntity> send(query: BaseQuery): List<U>
}
