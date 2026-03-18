package com.voiceai.routes

import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import com.voiceai.services.AudioProcessingService
import com.voiceai.models.EffectRequest

fun Route.audioRoutes() {
    val processingService = AudioProcessingService()

    post("/api/audio/effect") {
        val request = call.receive<EffectRequest>()
        val result = processingService.applyEffect(
            request.inputPath,
            request.outputFileName,
            request.effect
        )
        call.respond(HttpStatusCode.OK, result)
    }
}
