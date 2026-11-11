package io.nikhil.cqrsbankaccount.command.api.dtos

import io.nikhil.cqrsbankaccount.common.dtos.AccountType
import java.math.BigDecimal

data class OpenAccountRequest(
    val accountHolder: String,
    val accountType: AccountType,
    val openingBalance: BigDecimal = BigDecimal.ZERO,
)
