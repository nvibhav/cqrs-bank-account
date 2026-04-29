package io.nikhil.cqrsbankaccount

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CqrsBankAccountApplication

fun main(args: Array<String>) {
    runApplication<CqrsBankAccountApplication>(*args)
}
