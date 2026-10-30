package io.nikhil.cqrsbankaccount.core.infra

import io.nikhil.cqrsbankaccount.core.commands.BaseCommand
import io.nikhil.cqrsbankaccount.core.commands.CommandHandlerMethod

// Mediator
interface CommandDispatcher {
    fun <T : BaseCommand> registerHandler(
        type: Class<T>,
        handler: CommandHandlerMethod<T>
    )

    fun send(command: BaseCommand)
}
