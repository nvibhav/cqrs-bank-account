package io.nikhil.cqrsbankaccount.core.infra

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Configuration(proxyBeanMethods = false)
class MongoTransactionConfiguration {
    @Bean
    fun mongoTransactionManager(databaseFactory: MongoDatabaseFactory) =
        MongoTransactionManager(databaseFactory)

    @Bean
    fun mongoTransactionTemplate(transactionManager: MongoTransactionManager) =
        TransactionTemplate(transactionManager)
}
