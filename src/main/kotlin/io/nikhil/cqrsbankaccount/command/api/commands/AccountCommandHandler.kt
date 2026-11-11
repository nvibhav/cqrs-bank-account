package io.nikhil.cqrsbankaccount.command.api.commands

import io.nikhil.cqrsbankaccount.command.domain.AccountAggregate
import io.nikhil.cqrsbankaccount.core.commands.BaseCommand
import io.nikhil.cqrsbankaccount.core.events.BaseEvent
import io.nikhil.cqrsbankaccount.core.infra.EventStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.UUID

@Component
class AccountCommandHandler(
    private val eventStore: EventStore,
    @Qualifier("accountEventChannel") private val eventChannel: MessageChannel,
) : CommandHandler {
    @ServiceActivator(inputChannel = "accountCommandChannel")
    override fun handle(command: BaseCommand) {
        if (command is RestoreReadDbCommand) {
            restoreReadModel()
            return
        }

        val commandId = command.id?.takeIf(String::isNotBlank) ?: UUID.randomUUID().toString()
        command.id = commandId
        val fingerprint = fingerprint(command)
        val priorResult = eventStore.findCommandResult(commandId, fingerprint)
        if (priorResult != null) {
            if (command is OpenAccountCommand) command.accountId = priorResult
            return
        }

        when (command) {
            is OpenAccountCommand -> openAccount(command)
            is DepositFundsCommand -> updateAccount(command, command.accountId) { it.deposit(command.amount) }
            is WithdrawFundsCommand -> updateAccount(command, command.accountId) { it.withdraw(command.amount) }
            is CloseAccountCommand -> updateAccount(command, command.accountId) { it.close() }
            else -> throw IllegalArgumentException("Unsupported command: ${command.javaClass.simpleName}")
        }
    }

    private fun openAccount(command: OpenAccountCommand) {
        val accountId = command.accountId ?: UUID.randomUUID().toString()
        command.accountId = accountId
        val aggregate = AccountAggregate.open(
            accountId,
            command.accountHolder,
            command.accountType,
            command.openingBalance,
        )
        command.accountId = persist(command, accountId, aggregate.uncommittedEvents, expectedVersion = -1)
    }

    private fun updateAccount(command: BaseCommand, accountId: String, update: (AccountAggregate) -> Unit) {
        val history = eventStore.getEvents(accountId)
        if (history.isEmpty()) throw AccountNotFoundException(accountId)

        val aggregate = AccountAggregate.rehydrate(accountId, history)
        val expectedVersion = aggregate.version
        update(aggregate)
        persist(command, accountId, aggregate.uncommittedEvents, expectedVersion)
    }

    private fun persist(
        command: BaseCommand,
        accountId: String,
        events: List<BaseEvent>,
        expectedVersion: Int,
    ): String {
        val commandId = requireNotNull(command.id) { "Command ID is required." }
        return eventStore.saveEvents(
            accountId,
            events,
            expectedVersion,
            commandId,
            fingerprint(command),
        )
    }

    private fun fingerprint(command: BaseCommand): String {
        val payload = when (command) {
            is OpenAccountCommand -> listOf(
                "open",
                command.accountHolder.trim(),
                command.accountType.name,
                command.openingBalance.stripTrailingZeros().toPlainString(),
            ).joinToString("\u0000")
            is DepositFundsCommand -> listOf(
                "deposit",
                command.accountId,
                command.amount.stripTrailingZeros().toPlainString(),
            ).joinToString("\u0000")
            is WithdrawFundsCommand -> listOf(
                "withdraw",
                command.accountId,
                command.amount.stripTrailingZeros().toPlainString(),
            ).joinToString("\u0000")
            is CloseAccountCommand -> "close\u0000${command.accountId}"
            else -> throw IllegalArgumentException("Unsupported command: ${command.javaClass.simpleName}")
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { byte -> byte.toUByte().toString(16).padStart(2, '0') }
    }

    private fun restoreReadModel() {
        eventStore.getAggregateIds()
            .flatMap(eventStore::getEvents)
            .forEach { event ->
                check(eventChannel.send(GenericMessage(event))) { "The account event channel rejected an event." }
            }
    }
}

class AccountNotFoundException(accountId: String) : NoSuchElementException("Account $accountId was not found.")
