package io.nikhil.cqrsbankaccount.core.infra

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.config.EnableIntegration
import org.springframework.messaging.MessageChannel
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration(proxyBeanMethods = false)
@EnableIntegration
@EnableScheduling
class IntegrationConfiguration {
    @Bean
    fun accountCommandChannel(): MessageChannel = DirectChannel()

    @Bean
    fun accountEventChannel(): MessageChannel = DirectChannel()
}
