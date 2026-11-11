package io.nikhil.cqrsbankaccount.common.events

import io.nikhil.cqrsbankaccount.core.events.BaseEvent

data class AccountClosedEvent(val accountId: String) : BaseEvent(aggregateId = accountId)
