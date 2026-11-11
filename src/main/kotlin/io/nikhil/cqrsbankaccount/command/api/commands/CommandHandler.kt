package io.nikhil.cqrsbankaccount.command.api.commands

import io.nikhil.cqrsbankaccount.core.commands.BaseCommand

interface CommandHandler {
    fun handle(command: BaseCommand)
}
