package io.nikhil.cqrsbankaccount.core.infra

import io.nikhil.cqrsbankaccount.core.commands.BaseCommand
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.MessageChannel
import org.springframework.stereotype.Component

@Component
class SpringIntegrationCommandDispatcher(
    @Qualifier("accountCommandChannel") private val commandChannel: MessageChannel,
) : CommandDispatcher {
    override fun send(command: BaseCommand) {
        check(commandChannel.send(MessageBuilder.withPayload(command).build())) {
            "The command channel did not accept the command."
        }
    }
}
