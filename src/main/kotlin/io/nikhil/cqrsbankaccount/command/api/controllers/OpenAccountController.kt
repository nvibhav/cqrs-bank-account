package io.nikhil.cqrsbankaccount.command.api.controllers

import io.nikhil.cqrsbankaccount.command.api.commands.OpenAccountCommand
import io.nikhil.cqrsbankaccount.command.api.dtos.OpenAccountResponse
import io.nikhil.cqrsbankaccount.common.dtos.BaseResponse
import io.nikhil.cqrsbankaccount.core.infra.CommandDispatcher
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/v1/open-bank-account")
class OpenAccountController(val commandDispatcher: CommandDispatcher) {
    private val log = LoggerFactory.getLogger(OpenAccountController::class.java)

    @PostMapping
    fun openAccount(@RequestBody command: OpenAccountCommand): ResponseEntity<BaseResponse> {
        val id = UUID.randomUUID().toString()
        command.id = id
        try {
            commandDispatcher.send(command)
            return ResponseEntity(
                OpenAccountResponse("Bank account creation request completed successfully!", id),
                HttpStatus.CREATED
            )
        } catch (e: IllegalStateException) {
            log.error("Client made a bad request - {}", e.message)
            return ResponseEntity(BaseResponse(e.message), HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            log.atError().addKeyValue("id", id)
                .log("Error while processing request to open bank account.", e)
            return ResponseEntity(
                OpenAccountResponse("Error while processing request to open bank account.", id),
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }
}
