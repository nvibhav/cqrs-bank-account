package io.nikhil.cqrsbankaccount.core.events

import io.nikhil.cqrsbankaccount.core.messages.Message

abstract class BaseEvent(var version: Int = 0) : Message()
