package io.nikhil.cqrsbankaccount.command.api.controllers

import io.nikhil.cqrsbankaccount.command.api.commands.DepositFundsCommand
import io.nikhil.cqrsbankaccount.command.api.commands.WithdrawFundsCommand
import io.nikhil.cqrsbankaccount.command.api.dtos.FundsRequest
import io.nikhil.cqrsbankaccount.common.dtos.BaseResponse
import io.nikhil.cqrsbankaccount.core.infra.CommandDispatcher
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/accounts/{id}")
class FundsController(private val commandDispatcher: CommandDispatcher) {
    @PostMapping("/deposits")
    fun deposit(
        @PathVariable id: String,
        @RequestBody request: FundsRequest,
        @RequestHeader("Idempotency-Key", required = false) idempotencyKey: String?,
    ): ResponseEntity<BaseResponse> {
        commandDispatcher.send(DepositFundsCommand(id, request.amount).apply { this.id = idempotencyKey })
        return ResponseEntity.ok(BaseResponse("Funds deposited successfully."))
    }

    @PostMapping("/withdrawals")
    fun withdraw(
        @PathVariable id: String,
        @RequestBody request: FundsRequest,
        @RequestHeader("Idempotency-Key", required = false) idempotencyKey: String?,
    ): ResponseEntity<BaseResponse> {
        commandDispatcher.send(WithdrawFundsCommand(id, request.amount).apply { this.id = idempotencyKey })
        return ResponseEntity.ok(BaseResponse("Funds withdrawn successfully."))
    }
}
