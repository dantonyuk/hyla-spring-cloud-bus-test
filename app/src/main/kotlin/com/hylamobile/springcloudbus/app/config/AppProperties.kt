package com.hylamobile.springcloudbus.app.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("app")
class AppProperties {
    lateinit var message: String
}
