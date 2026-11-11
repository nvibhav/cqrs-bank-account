package io.nikhil.cqrsbankaccount.query.api.controllers

import io.nikhil.cqrsbankaccount.command.api.commands.AccountNotFoundException
import io.nikhil.cqrsbankaccount.query.api.models.AccountView
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/accounts")
class AccountQueryController(private val mongoTemplate: MongoTemplate) {
    @GetMapping
    fun listAccounts(): List<AccountView> =
        mongoTemplate.find(
            Query().with(Sort.by(Sort.Direction.ASC, "createdAt")),
            AccountView::class.java,
        )

    @GetMapping("/{id}")
    fun getAccount(@PathVariable id: String): AccountView =
        mongoTemplate.findOne(
            Query.query(Criteria.where("_id").`is`(id)),
            AccountView::class.java,
        ) ?: throw AccountNotFoundException(id)
}
