package io.nikhil.cqrsbankaccount.command.api.controllers

import io.nikhil.cqrsbankaccount.command.api.commands.CloseAccountCommand
import io.nikhil.cqrsbankaccount.common.dtos.BaseResponse
import io.nikhil.cqrsbankaccount.core.infra.CommandDispatcher
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/close-bank-account")
class CloseAccountController(private val commandDispatcher: CommandDispatcher) {
    @DeleteMapping("/{id}")
    fun closeAccount(
        @PathVariable id: String,
        @RequestHeader("Idempotency-Key", required = false) idempotencyKey: String?,
    ): ResponseEntity<BaseResponse> {
        commandDispatcher.send(CloseAccountCommand(id).apply { this.id = idempotencyKey })
        return ResponseEntity(BaseResponse("Bank account closed successfully."), HttpStatus.OK)
    }
}
