package io.nikhil.cqrsbankaccount.query.api.projections

import io.nikhil.cqrsbankaccount.core.infra.EventStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.MessageChannel
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.projection.enabled"], havingValue = "true", matchIfMissing = true)
class AccountProjectionWorker(
    private val eventStore: EventStore,
    private val accountProjection: AccountProjection,
    @Qualifier("accountEventChannel") private val eventChannel: MessageChannel,
) {
    private val log = LoggerFactory.getLogger(AccountProjectionWorker::class.java)

    @Scheduled(fixedDelayString = "\${app.projection.poll-interval-ms:1000}")
    fun projectPendingEvents() {
        var firstFailure: RuntimeException? = null

        eventStore.getAggregateIds().forEach { accountId ->
            try {
                val appliedVersion = accountProjection.getAppliedVersion(accountId)
                eventStore.getEventsAfter(accountId, appliedVersion).forEach { event ->
                    check(eventChannel.send(MessageBuilder.withPayload(event).build())) {
                        "The account event channel rejected event version ${event.version} for $accountId."
                    }
                }
            } catch (exception: RuntimeException) {
                log.error("Failed to project events for account {}; they will be retried.", accountId, exception)
                if (firstFailure == null) firstFailure = exception
            }
        }

        firstFailure?.let { throw it }
    }
}
