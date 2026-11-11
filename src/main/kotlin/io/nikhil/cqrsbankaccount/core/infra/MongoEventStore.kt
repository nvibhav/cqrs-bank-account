package io.nikhil.cqrsbankaccount.core.infra

import io.nikhil.cqrsbankaccount.core.events.BaseEvent
import io.nikhil.cqrsbankaccount.core.events.CommandReceipt
import io.nikhil.cqrsbankaccount.core.events.EventModel
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.atomic.AtomicBoolean

@Repository
class MongoEventStore(
    private val mongoTemplate: MongoTemplate,
    private val transactionTemplate: TransactionTemplate,
) : EventStore {
    private val indexesReady = AtomicBoolean(false)

    override fun saveEvents(
        aggregateId: String,
        events: Iterable<BaseEvent>,
        expectedVersion: Int,
        commandId: String,
        commandFingerprint: String,
    ): String {
        val newEvents = events.toList()
        require(newEvents.isNotEmpty()) { "Cannot record a command without events." }

        ensureIndexes()

        try {
            return transactionTemplate.execute {
                val receipt = mongoTemplate.findById(commandId, CommandReceipt::class.java)
                if (receipt != null) {
                    check(receipt.fingerprint == commandFingerprint) {
                        "Idempotency key was already used for a different command."
                    }
                    return@execute receipt.aggregateIdentifier
                }

                val latest = mongoTemplate.find(
                    Query.query(Criteria.where("aggregateIdentifier").`is`(aggregateId))
                        .with(Sort.by(Sort.Direction.DESC, "version"))
                        .limit(1),
                    EventModel::class.java,
                ).firstOrNull()?.version ?: -1

                check(latest == expectedVersion) {
                    "Account $aggregateId changed concurrently; expected version $expectedVersion but found $latest."
                }

                newEvents.forEachIndexed { index, event ->
                    event.aggregateId = aggregateId
                    event.version = expectedVersion + index + 1
                    mongoTemplate.insert(
                        EventModel(
                            aggregateIdentifier = aggregateId,
                            aggregateType = "Account",
                            version = event.version,
                            eventType = event.javaClass.simpleName,
                            eventData = event,
                        ),
                    )
                }
                mongoTemplate.insert(CommandReceipt(commandId, aggregateId, commandFingerprint))
                aggregateId
            }
        } catch (exception: DuplicateKeyException) {
            val receipt = mongoTemplate.findById(commandId, CommandReceipt::class.java)
            if (receipt != null && receipt.fingerprint == commandFingerprint) {
                return receipt.aggregateIdentifier
            }
            if (receipt != null) {
                throw IllegalStateException("Idempotency key was already used for a different command.", exception)
            }
            throw IllegalStateException("Account $aggregateId changed concurrently; reload and retry.", exception)
        }
    }

    override fun findCommandResult(commandId: String, commandFingerprint: String): String? {
        val receipt = mongoTemplate.findById(commandId, CommandReceipt::class.java) ?: return null
        check(receipt.fingerprint == commandFingerprint) {
            "Idempotency key was already used for a different command."
        }
        return receipt.aggregateIdentifier
    }

    override fun getEvents(aggregateId: String): List<BaseEvent> =
        mongoTemplate.find(
            Query.query(Criteria.where("aggregateIdentifier").`is`(aggregateId))
                .with(Sort.by(Sort.Direction.ASC, "version")),
            EventModel::class.java,
        ).map { model ->
            requireNotNull(model.eventData) { "Event ${model.id} has no event data." }
        }

    override fun getEventsAfter(aggregateId: String, version: Int): List<BaseEvent> =
        mongoTemplate.find(
            Query.query(
                Criteria.where("aggregateIdentifier").`is`(aggregateId)
                    .and("version").gt(version),
            ).with(Sort.by(Sort.Direction.ASC, "version")),
            EventModel::class.java,
        ).map { model ->
            requireNotNull(model.eventData) { "Event ${model.id} has no event data." }
        }

    override fun getAggregateIds(): List<String> =
        mongoTemplate.findDistinct(
            Query(),
            "aggregateIdentifier",
            EventModel::class.java,
            String::class.java,
        )

    private fun ensureIndexes() {
        if (indexesReady.get()) return
        synchronized(indexesReady) {
            if (indexesReady.get()) return
            mongoTemplate.indexOps(EventModel::class.java).createIndex(
                Index()
                    .on("aggregateIdentifier", Sort.Direction.ASC)
                    .on("version", Sort.Direction.ASC)
                    .unique()
                    .named("aggregate_version_unique"),
            )
            indexesReady.set(true)
        }
    }
}
