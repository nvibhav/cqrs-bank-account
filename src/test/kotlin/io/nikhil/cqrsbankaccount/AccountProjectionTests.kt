package io.nikhil.cqrsbankaccount

import io.nikhil.cqrsbankaccount.common.dtos.AccountType
import io.nikhil.cqrsbankaccount.common.events.AccountOpenedEvent
import io.nikhil.cqrsbankaccount.common.events.FundsDepositedEvent
import io.nikhil.cqrsbankaccount.core.infra.EventStore
import io.nikhil.cqrsbankaccount.query.api.models.AccountView
import io.nikhil.cqrsbankaccount.query.api.projections.AccountProjection
import io.nikhil.cqrsbankaccount.query.api.projections.AccountProjectionWorker
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.integration.channel.DirectChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals

class AccountProjectionTests {
    @Test
    fun `projection ignores an already applied event`() {
        val mongoTemplate = Mockito.mock(MongoTemplate::class.java)
        val account = accountView(version = 1)
        `when`(mongoTemplate.findById("account-1", AccountView::class.java)).thenReturn(account)

        AccountProjection(mongoTemplate).project(
            FundsDepositedEvent("account-1", BigDecimal("5")).apply { version = 1 },
        )

        verify(mongoTemplate, never()).updateFirst(
            any(Query::class.java),
            any(Update::class.java),
            eq(AccountView::class.java),
        )
    }

    @Test
    fun `projection rejects a version gap without changing the read model`() {
        val mongoTemplate = Mockito.mock(MongoTemplate::class.java)
        `when`(mongoTemplate.findById("account-1", AccountView::class.java)).thenReturn(accountView(version = 1))

        val event = FundsDepositedEvent("account-1", BigDecimal("5")).apply { version = 3 }
        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            AccountProjection(mongoTemplate).project(event)
        }

        verify(mongoTemplate, never()).updateFirst(
            any(Query::class.java),
            any(Update::class.java),
            eq(AccountView::class.java),
        )
    }

    @Test
    fun `projection replays the opening event for a legacy view without a checkpoint`() {
        val mongoTemplate = Mockito.mock(MongoTemplate::class.java)
        `when`(mongoTemplate.findById("account-1", AccountView::class.java)).thenReturn(accountView(version = -1))
        Mockito.`when`(
            mongoTemplate.updateFirst(
                any(Query::class.java),
                any(Update::class.java),
                eq(AccountView::class.java),
            ),
        ).thenReturn(com.mongodb.client.result.UpdateResult.acknowledged(1, 1L, null))

        AccountProjection(mongoTemplate).project(
            AccountOpenedEvent(
                accountHolder = "Ada Lovelace",
                accountType = AccountType.SAVINGS,
                openingBalance = BigDecimal("10"),
                accountId = "account-1",
            ).apply { version = 0 },
        )

        verify(mongoTemplate).updateFirst(
            any(Query::class.java),
            any(Update::class.java),
            eq(AccountView::class.java),
        )
    }

    @Test
    fun `worker publishes events after the stored projection version`() {
        val eventStore = Mockito.mock(EventStore::class.java)
        val accountProjection = Mockito.mock(AccountProjection::class.java)
        val accountEvent = FundsDepositedEvent("account-1", BigDecimal("5")).apply { version = 2 }
        `when`(eventStore.getAggregateIds()).thenReturn(listOf("account-1"))
        `when`(accountProjection.getAppliedVersion("account-1")).thenReturn(1)
        `when`(eventStore.getEventsAfter("account-1", 1)).thenReturn(listOf(accountEvent))

        val channel = DirectChannel()
        val delivered = mutableListOf<Message<*>>()
        channel.subscribe(MessageHandler { delivered += it })

        AccountProjectionWorker(eventStore, accountProjection, channel).projectPendingEvents()

        assertEquals(1, delivered.size)
        assertEquals(accountEvent, delivered.single().payload)
        verify(eventStore).getEventsAfter(anyString(), anyInt())
    }

    private fun accountView(version: Int) = AccountView(
        id = "account-1",
        accountHolder = "Ada Lovelace",
        accountType = AccountType.SAVINGS,
        balance = BigDecimal("15"),
        createdAt = Instant.EPOCH,
        version = version,
    )
}
