package io.nikhil.cqrsbankaccount.common.events

import io.nikhil.cqrsbankaccount.core.events.BaseEvent

data class FundsDepositedEvent(
    var amount: Double = 0.0,
) : BaseEvent() {
}
