package io.nikhil.cqrsbankaccount.core.infra

import io.nikhil.cqrsbankaccount.core.commands.BaseCommand

// Mediator
interface CommandDispatcher {
    fun send(command: BaseCommand)
}
