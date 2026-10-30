package io.nikhil.cqrsbankaccount.command.api.commands

import io.nikhil.cqrsbankaccount.core.commands.BaseCommand

data class DepositFundsCommand(
    var amount: Double? = 0.0
) : BaseCommand() {
}
