package com.voiceai

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.voiceai.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSerialization()
        configureAuthentication()
    }.start(wait = true)
}

fun Application.module() {
    configureRouting()
    configureSerialization()
    configureAuthentication()
}
