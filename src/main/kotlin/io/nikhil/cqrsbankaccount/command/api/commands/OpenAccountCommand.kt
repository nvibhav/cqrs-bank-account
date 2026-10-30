package io.nikhil.cqrsbankaccount.command.api.commands

import io.nikhil.cqrsbankaccount.common.dtos.AccountType
import io.nikhil.cqrsbankaccount.core.commands.BaseCommand

data class OpenAccountCommand(
    var accountHolder: String? = null,
    var accountType: AccountType? = null,
    var openingBalance: Double? = 0.0,
) : BaseCommand() {
}
