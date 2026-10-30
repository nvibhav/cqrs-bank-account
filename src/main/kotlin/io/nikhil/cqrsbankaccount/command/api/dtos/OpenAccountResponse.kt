package io.nikhil.cqrsbankaccount.command.api.dtos

import io.nikhil.cqrsbankaccount.common.dtos.BaseResponse

data class OpenAccountResponse(
    val id: String,
) : BaseResponse() {
    constructor(message: String, id: String) : this(id) {
        this.message = message
    }
}
