package io.nikhil.cqrsbankaccount

import io.nikhil.cqrsbankaccount.common.dtos.AccountType
import io.nikhil.cqrsbankaccount.common.events.AccountOpenedEvent
import io.nikhil.cqrsbankaccount.common.events.FundsDepositedEvent
import io.nikhil.cqrsbankaccount.core.events.CommandReceipt
import io.nikhil.cqrsbankaccount.core.events.EventModel
import io.nikhil.cqrsbankaccount.core.infra.EventStore
import io.nikhil.cqrsbankaccount.query.api.models.AccountView
import io.nikhil.cqrsbankaccount.query.api.projections.AccountProjection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mongodb.MongoDBContainer
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = ["app.projection.enabled=false"])
class MongoPersistenceTests {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var eventStore: EventStore

    @Autowired
    private lateinit var accountProjection: AccountProjection

    @BeforeEach
    fun clearCollections() {
        mongoTemplate.remove(Query(), EventModel::class.java)
        mongoTemplate.remove(Query(), CommandReceipt::class.java)
        mongoTemplate.remove(Query(), AccountView::class.java)
    }

    @Test
    fun `event store appends events and receipts and reads events in order`() {
        val opening = AccountOpenedEvent(
            accountHolder = "Ada Lovelace",
            accountType = AccountType.SAVINGS,
            openingBalance = BigDecimal("10.00"),
            createdAt = Instant.EPOCH,
            accountId = "account-1",
        )
        val deposit = FundsDepositedEvent("account-1", BigDecimal("5.00"))

        eventStore.saveEvents("account-1", listOf(opening, deposit), -1, "open-command", "open-fingerprint")

        assertEquals(listOf(0, 1), eventStore.getEvents("account-1").map { it.version })
        assertEquals(listOf(1), eventStore.getEventsAfter("account-1", 0).map { it.version })
        assertEquals("account-1", eventStore.findCommandResult("open-command", "open-fingerprint"))
        assertEquals(2, mongoTemplate.count(Query(), EventModel::class.java))
    }

    @Test
    fun `event store rejects an idempotency key with a different fingerprint`() {
        eventStore.saveEvents(
            "account-1",
            listOf(
                AccountOpenedEvent(
                    accountHolder = "Ada Lovelace",
                    accountType = AccountType.SAVINGS,
                    openingBalance = BigDecimal.ZERO,
                    createdAt = Instant.EPOCH,
                    accountId = "account-1",
                ),
            ),
            -1,
            "same-key",
            "original-fingerprint",
        )

        assertFailsWith<IllegalStateException> {
            eventStore.saveEvents(
                "account-1",
                listOf(FundsDepositedEvent("account-1", BigDecimal.ONE)),
                0,
                "same-key",
                "different-fingerprint",
            )
        }

        assertEquals(1, mongoTemplate.count(Query(), EventModel::class.java))
        assertNull(eventStore.findCommandResult("missing-key", "fingerprint"))
    }

    @Test
    fun `event store treats a matching idempotency key as an already committed command`() {
        val opening = AccountOpenedEvent(
            accountHolder = "Ada Lovelace",
            accountType = AccountType.SAVINGS,
            openingBalance = BigDecimal.ZERO,
            createdAt = Instant.EPOCH,
            accountId = "account-1",
        )
        eventStore.saveEvents("account-1", listOf(opening), -1, "same-key", "same-fingerprint")

        val result = eventStore.saveEvents(
            "account-1",
            listOf(FundsDepositedEvent("account-1", BigDecimal("100"))),
            0,
            "same-key",
            "same-fingerprint",
        )

        assertEquals("account-1", result)
        assertEquals(1, mongoTemplate.count(Query(), EventModel::class.java))
    }

    @Test
    fun `event store rejects a stale expected version without appending events`() {
        eventStore.saveEvents(
            "account-1",
            listOf(
                AccountOpenedEvent(
                    accountHolder = "Ada Lovelace",
                    accountType = AccountType.SAVINGS,
                    openingBalance = BigDecimal.ZERO,
                    createdAt = Instant.EPOCH,
                    accountId = "account-1",
                ),
            ),
            -1,
            "open-command",
            "open-fingerprint",
        )

        assertFailsWith<IllegalStateException> {
            eventStore.saveEvents(
                "account-1",
                listOf(FundsDepositedEvent("account-1", BigDecimal.ONE)),
                -1,
                "stale-command",
                "stale-fingerprint",
            )
        }

        assertEquals(1, mongoTemplate.count(Query(), EventModel::class.java))
        assertNull(eventStore.findCommandResult("stale-command", "stale-fingerprint"))
    }

    @Test
    fun `projection persists balances and ignores duplicate event delivery`() {
        val opened = AccountOpenedEvent(
            accountHolder = "Ada Lovelace",
            accountType = AccountType.CURRENT,
            openingBalance = BigDecimal("10.00"),
            createdAt = Instant.EPOCH,
            accountId = "account-1",
        ).apply { version = 0 }
        val deposit = FundsDepositedEvent("account-1", BigDecimal("5.00")).apply { version = 1 }

        accountProjection.project(opened)
        accountProjection.project(deposit)
        accountProjection.project(deposit)

        val account = requireNotNull(mongoTemplate.findById("account-1", AccountView::class.java))
        assertEquals(BigDecimal("15.00"), account.balance)
        assertEquals(1, account.version)
        assertEquals(1, accountProjection.getAppliedVersion("account-1"))
    }

    companion object {
        @Container
        @JvmField
        val mongo = MongoDBContainer("mongo:8")
            .withReplicaSet()
            .withStartupTimeout(Duration.ofMinutes(2))
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger("MongoDB Testcontainer")))

        @JvmStatic
        @DynamicPropertySource
        fun registerMongoProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl)
        }
    }
}
