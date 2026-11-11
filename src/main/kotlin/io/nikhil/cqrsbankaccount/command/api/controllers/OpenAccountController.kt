package io.nikhil.cqrsbankaccount.command.api.controllers

import io.nikhil.cqrsbankaccount.command.api.commands.OpenAccountCommand
import io.nikhil.cqrsbankaccount.command.api.dtos.OpenAccountRequest
import io.nikhil.cqrsbankaccount.command.api.dtos.OpenAccountResponse
import io.nikhil.cqrsbankaccount.core.infra.CommandDispatcher
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/v1/open-bank-account")
class OpenAccountController(private val commandDispatcher: CommandDispatcher) {
    @PostMapping
    fun openAccount(
        @RequestBody request: OpenAccountRequest,
        @RequestHeader("Idempotency-Key", required = false) idempotencyKey: String?,
    ): ResponseEntity<OpenAccountResponse> {
        val command = OpenAccountCommand(
            request.accountHolder,
            request.accountType,
            request.openingBalance,
            accountId = UUID.randomUUID().toString(),
        ).apply { id = idempotencyKey }
        commandDispatcher.send(command)
        val id = requireNotNull(command.accountId) { "Account command completed without an account ID." }
        return ResponseEntity(
            OpenAccountResponse("Bank account opened successfully.", id),
            HttpStatus.CREATED,
        )
    }
}
