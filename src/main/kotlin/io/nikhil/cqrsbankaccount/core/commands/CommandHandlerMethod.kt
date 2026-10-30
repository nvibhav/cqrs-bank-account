package io.nikhil.cqrsbankaccount.core.commands

fun interface CommandHandlerMethod<T : BaseCommand> {
    fun handle(command: T)
}
