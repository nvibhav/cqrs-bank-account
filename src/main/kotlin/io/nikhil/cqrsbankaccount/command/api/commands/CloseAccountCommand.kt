package io.nikhil.cqrsbankaccount.command.api.commands

import io.nikhil.cqrsbankaccount.core.commands.BaseCommand

class CloseAccountCommand() : BaseCommand() {
    constructor(id: String) : super(id)
}
