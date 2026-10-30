package io.nikhil.cqrsbankaccount.command.api.controllers

import io.nikhil.cqrsbankaccount.command.api.commands.CloseAccountCommand
import io.nikhil.cqrsbankaccount.common.dtos.BaseResponse
import io.nikhil.cqrsbankaccount.core.infra.CommandDispatcher
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/close-bank-account")
class CloseAccountController(val commandDispatcher: CommandDispatcher) {
    private val log = LoggerFactory.getLogger(CloseAccountController::class.java)

    @DeleteMapping("/{id}")
    fun closeAccount(@PathVariable id: String): ResponseEntity<BaseResponse> {
        try {
            commandDispatcher.send(CloseAccountCommand(id))
            return ResponseEntity(
                BaseResponse("Bank account closure request completed successfully!"),
                HttpStatus.OK
            )
        } catch (e: IllegalStateException) {
            log.error("Client made a bad request - {}", e.message)
            return ResponseEntity(BaseResponse(e.message), HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            log.atError().addKeyValue("id", id)
                .log("Error while processing request to open bank account.", e)
            return ResponseEntity(
                BaseResponse("Error while processing request to open bank account for id $id."),
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }
}
