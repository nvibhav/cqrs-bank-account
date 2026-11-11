package io.nikhil.cqrsbankaccount

import io.nikhil.cqrsbankaccount.command.api.commands.AccountCommandHandler
import io.nikhil.cqrsbankaccount.command.api.commands.OpenAccountCommand
import io.nikhil.cqrsbankaccount.common.dtos.AccountType
import io.nikhil.cqrsbankaccount.core.infra.EventStore
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.messaging.MessageChannel
import java.math.BigDecimal
import kotlin.test.assertEquals

class AccountCommandHandlerTests {
    @Test
    fun `retry of an open command reuses its original account id`() {
        val eventStore = Mockito.mock(EventStore::class.java)
        val eventChannel = Mockito.mock(MessageChannel::class.java)
        `when`(eventStore.findCommandResult(anyString(), anyString())).thenReturn("original-account")
        val command = OpenAccountCommand(
            accountHolder = "Ada Lovelace",
            accountType = AccountType.SAVINGS,
            openingBalance = BigDecimal("25.00"),
            accountId = "newly-generated-account",
        ).apply { id = "request-123" }

        AccountCommandHandler(eventStore, eventChannel).handle(command)

        assertEquals("original-account", command.accountId)
        verify(eventStore, never()).saveEvents(
            anyString(),
            Mockito.anyList(),
            Mockito.anyInt(),
            anyString(),
            anyString(),
        )
    }
}
