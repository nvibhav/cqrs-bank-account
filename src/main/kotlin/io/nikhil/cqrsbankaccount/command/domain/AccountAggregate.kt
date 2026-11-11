package io.nikhil.cqrsbankaccount.command.domain

import io.nikhil.cqrsbankaccount.common.dtos.AccountType
import io.nikhil.cqrsbankaccount.common.events.AccountClosedEvent
import io.nikhil.cqrsbankaccount.common.events.AccountOpenedEvent
import io.nikhil.cqrsbankaccount.common.events.FundsDepositedEvent
import io.nikhil.cqrsbankaccount.common.events.FundsWithdrawnEvent
import io.nikhil.cqrsbankaccount.core.events.BaseEvent
import java.math.BigDecimal

class AccountAggregate private constructor(private val accountId: String) {
    var version: Int = -1
        private set
    var balance: BigDecimal = BigDecimal.ZERO
        private set
    var isOpen: Boolean = false
        private set

    private val pendingEvents = mutableListOf<BaseEvent>()
    val uncommittedEvents: List<BaseEvent>
        get() = pendingEvents.toList()

    fun deposit(amount: BigDecimal) {
        requirePositive(amount)
        requireOpen()
        record(FundsDepositedEvent(accountId, amount))
    }

    fun withdraw(amount: BigDecimal) {
        requirePositive(amount)
        requireOpen()
        check(balance >= amount) { "Insufficient funds." }
        record(FundsWithdrawnEvent(accountId, amount))
    }

    fun close() {
        requireOpen()
        check(balance.compareTo(BigDecimal.ZERO) == 0) { "An account must have a zero balance before it can be closed." }
        record(AccountClosedEvent(accountId))
    }

    private fun requireOpen() {
        check(isOpen) { "The account is closed or has not been opened." }
    }

    private fun record(event: BaseEvent) {
        event.version = version + 1
        apply(event)
        pendingEvents += event
    }

    private fun apply(event: BaseEvent) {
        check(event.aggregateId == accountId) { "Event belongs to a different account." }
        check(event.version == version + 1) { "Account event version is out of sequence." }

        when (event) {
            is AccountOpenedEvent -> {
                check(!isOpen && version == -1) { "The account has already been opened." }
                require(event.accountHolder.isNotBlank()) { "Account holder is required." }
                require(event.openingBalance >= BigDecimal.ZERO) { "Opening balance cannot be negative." }
                balance = event.openingBalance
                isOpen = true
            }
            is FundsDepositedEvent -> {
                check(isOpen) { "The account is closed or has not been opened." }
                requirePositive(event.amount)
                balance += event.amount
            }
            is FundsWithdrawnEvent -> {
                check(isOpen) { "The account is closed or has not been opened." }
                requirePositive(event.amount)
                check(balance >= event.amount) { "Insufficient funds." }
                balance -= event.amount
            }
            is AccountClosedEvent -> {
                check(isOpen) { "The account is closed or has not been opened." }
                check(balance.compareTo(BigDecimal.ZERO) == 0) {
                    "An account must have a zero balance before it can be closed."
                }
                isOpen = false
            }
            else -> error("Unsupported account event: ${event.javaClass.simpleName}")
        }

        version = event.version
    }

    companion object {
        fun open(
            accountId: String,
            accountHolder: String,
            accountType: AccountType,
            openingBalance: BigDecimal,
        ): AccountAggregate {
            require(accountId.isNotBlank()) { "Account ID is required." }
            require(accountHolder.isNotBlank()) { "Account holder is required." }
            require(openingBalance >= BigDecimal.ZERO) { "Opening balance cannot be negative." }
            return AccountAggregate(accountId).apply {
                record(AccountOpenedEvent(accountHolder.trim(), accountType, openingBalance, accountId = accountId))
            }
        }

        fun rehydrate(accountId: String, history: List<BaseEvent>): AccountAggregate {
            check(history.isNotEmpty()) { "No events found for account $accountId." }
            return AccountAggregate(accountId).apply { history.forEach(::apply) }
        }

        private fun requirePositive(amount: BigDecimal) {
            require(amount > BigDecimal.ZERO) { "Amount must be greater than zero." }
        }
    }
}
