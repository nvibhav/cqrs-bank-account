package io.nikhil.cqrsbankaccount.common.events

import io.nikhil.cqrsbankaccount.core.events.BaseEvent
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.math.BigDecimal

data class FundsDepositedEvent(
    val accountId: String,
    @field:Field(targetType = FieldType.DECIMAL128)
    val amount: BigDecimal,
) : BaseEvent(aggregateId = accountId)
