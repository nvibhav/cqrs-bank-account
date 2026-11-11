package io.nikhil.cqrsbankaccount.query.api.models

import io.nikhil.cqrsbankaccount.common.dtos.AccountType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.math.BigDecimal
import java.time.Instant

@Document("accounts")
data class AccountView(
    @Id val id: String,
    val accountHolder: String,
    val accountType: AccountType,
    @field:Field(targetType = FieldType.DECIMAL128)
    val balance: BigDecimal,
    val createdAt: Instant,
    val closed: Boolean = false,
    val version: Int = -1,
)
