package io.nikhil.cqrsbankaccount.core.commands

import io.nikhil.cqrsbankaccount.core.messages.Message

abstract class BaseCommand(
    id: String? = null
) : Message(id) {
}
