package io.nikhil.cqrsbankaccount.command.api.commands

interface CommandHandler {
    fun handle(command: OpenAccountCommand)
    fun handle(command: DepositFundsCommand)
    fun handle(command: WithdrawFundsCommand)
    fun handle(command: CloseAccountCommand)
    fun handle(command: RestoreReadDbCommand)
}
