package io.nikhil.cqrsbankaccount.command.api.commands

import io.nikhil.cqrsbankaccount.core.commands.BaseCommand

data class CloseAccountCommand(val accountId: String) : BaseCommand()
