package io.nikhil.cqrsbankaccount.common.events

import io.nikhil.cqrsbankaccount.common.dtos.AccountType
import io.nikhil.cqrsbankaccount.core.events.BaseEvent
import java.util.*

data class AccountOpenedEvent(
    var accountHolder: String? = null,
    var accountType: AccountType? = null,
    var createdDate: Date? = null,
    var openingBalance: Double = 0.0,
) : BaseEvent() {
}
