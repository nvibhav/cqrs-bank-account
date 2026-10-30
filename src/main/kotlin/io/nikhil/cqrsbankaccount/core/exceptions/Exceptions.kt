package io.nikhil.cqrsbankaccount.core.exceptions

class AggregateNotFoundException(message: String) : RuntimeException(message)

class ConcurrencyException : RuntimeException()
