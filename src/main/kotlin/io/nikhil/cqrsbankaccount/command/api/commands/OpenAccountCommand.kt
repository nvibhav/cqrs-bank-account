package io.nikhil.cqrsbankaccount.command.api.commands

import io.nikhil.cqrsbankaccount.common.dtos.AccountType
import io.nikhil.cqrsbankaccount.core.commands.BaseCommand
import java.math.BigDecimal

data class OpenAccountCommand(
    val accountHolder: String,
    val accountType: AccountType,
    val openingBalance: BigDecimal = BigDecimal.ZERO,
    var accountId: String? = null,
) : BaseCommand()
