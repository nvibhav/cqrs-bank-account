package io.nikhil.cqrsbankaccount.common.dtos

import io.nikhil.cqrsbankaccount.command.api.commands.AccountNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(AccountNotFoundException::class)
    fun accountNotFound(exception: AccountNotFoundException): ResponseEntity<BaseResponse> =
        ResponseEntity(BaseResponse(exception.message), HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidRequest(exception: IllegalArgumentException): ResponseEntity<BaseResponse> =
        ResponseEntity(BaseResponse(exception.message), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(IllegalStateException::class)
    fun invalidState(exception: IllegalStateException): ResponseEntity<BaseResponse> =
        ResponseEntity(BaseResponse(exception.message), HttpStatus.CONFLICT)
}
