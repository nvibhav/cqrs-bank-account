package io.nikhil.cqrsbankaccount

import io.nikhil.cqrsbankaccount.command.domain.AccountAggregate
import io.nikhil.cqrsbankaccount.common.dtos.AccountType
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AccountAggregateTests {
    @Test
    fun `deposit and withdrawal update balance and can be replayed`() {
        val account = AccountAggregate.open("account-1", "Ada Lovelace", AccountType.SAVINGS, BigDecimal("25.00"))
        account.deposit(BigDecimal("10.50"))
        account.withdraw(BigDecimal("5.25"))

        assertEquals(BigDecimal("30.25"), account.balance)

        val restored = AccountAggregate.rehydrate("account-1", account.uncommittedEvents)
        assertEquals(BigDecimal("30.25"), restored.balance)
        assertTrue(restored.isOpen)
    }

    @Test
    fun `withdrawal cannot overdraw account`() {
        val account = AccountAggregate.open("account-1", "Ada Lovelace", AccountType.SAVINGS, BigDecimal("10"))

        assertFailsWith<IllegalStateException> { account.withdraw(BigDecimal("10.01")) }
        assertEquals(BigDecimal("10"), account.balance)
    }

    @Test
    fun `amounts must be positive`() {
        val account = AccountAggregate.open("account-1", "Ada Lovelace", AccountType.CURRENT, BigDecimal.ZERO)

        assertFailsWith<IllegalArgumentException> { account.deposit(BigDecimal.ZERO) }
        assertFailsWith<IllegalArgumentException> { account.withdraw(BigDecimal("-1")) }
    }

    @Test
    fun `account can only close with zero balance`() {
        val account = AccountAggregate.open("account-1", "Ada Lovelace", AccountType.CURRENT, BigDecimal("1"))

        assertFailsWith<IllegalStateException> { account.close() }
        account.withdraw(BigDecimal("1"))
        account.close()

        assertFalse(account.isOpen)
        assertFailsWith<IllegalStateException> { account.deposit(BigDecimal.ONE) }
    }
}
