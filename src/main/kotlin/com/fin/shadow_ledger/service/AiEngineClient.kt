package com.fin.shadow_ledger.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.util.concurrent.CompletableFuture

@Service
class AiEngineClient(builder: RestTemplateBuilder) {

    private val logger = LoggerFactory.getLogger(AiEngineClient::class.java)

    @Value("\${ai.engine.url:http://localhost:5000}")
    private lateinit var aiUrl: String

    private val restTemplate: RestTemplate = builder
        .setConnectTimeout(Duration.ofSeconds(2))
        .setReadTimeout(Duration.ofSeconds(2))
        .build()

    fun analyzeAsync(payload: Map<String, Any>): CompletableFuture<Map<String, Any>> {
        return CompletableFuture.supplyAsync {
            try {
                val response = restTemplate.postForObject("$aiUrl/analyze", payload, Map::class.java)
                @Suppress("UNCHECKED_CAST")
                response as Map<String, Any>
            } catch (e: Exception) {
                logger.error("AI Engine Unreachable: ${e.message}")
                // Fallback: Assume Medium Risk if AI is dead (Fail Safe)
                mapOf(
                    "score" to 0.5,
                    "reasons" to listOf("AI_OFFLINE_FALLBACK")
                )
            }
        }
    }
}