package com.hylamobile.springcloudbus.app.web

import com.hylamobile.springcloudbus.app.config.AppProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AppController {

    companion object {
        private val logger = LoggerFactory.getLogger(AppController::class.qualifiedName)
    }

    @Autowired
    private lateinit var properties: AppProperties

    @GetMapping("/test")
    fun test(): Response {
        logger.debug("AppController::debugMessage")
        logger.info("AppController::infoMessage")
        return Response(properties.message)
    }
}

data class Response(var message: String)
