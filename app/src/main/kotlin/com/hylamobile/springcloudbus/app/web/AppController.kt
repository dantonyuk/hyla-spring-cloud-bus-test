package com.hylamobile.springcloudbus.app.web

import com.hylamobile.springcloudbus.app.config.AppProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AppController {

    @Autowired
    private lateinit var properties: AppProperties

    @GetMapping("/test")
    fun test() = Response(properties.message)
}

data class Response(var message: String)
