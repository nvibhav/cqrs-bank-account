package io.nikhil.cqrsbankaccount.command.api.commands

import io.nikhil.cqrsbankaccount.core.commands.BaseCommand
import java.math.BigDecimal

data class DepositFundsCommand(
    val accountId: String,
    val amount: BigDecimal,
) : BaseCommand()
