package com.fin.shadow_ledger.controller

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.service.AuditService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.time.Instant

// Data class to map incoming JSON from pay.html
// ... imports

// UPDATED Payload to include "Context"
data class PaymentRequest(
    val accountId: String, // Who is paying?
    val toAccount: String,
    val amount: Double,
    
    // Context for AI
    val currentLat: Double,
    val currentLon: Double,
    val deviceId: String,
    val ipAddress: String,
    val biometricToken: Boolean = false // Did they use FaceID?
)

// ... Rest of the controller

@RestController
@RequestMapping("/api/v1/audit")
class AuditController(private val auditService: AuditService) {

    @PostMapping("/payment")
    fun processPayment(@RequestBody request: PaymentRequest): ResponseEntity<Map<String, Any>> {
        println("💰 Payment Request: ₹${request.amount} | ${request.fromAccount} -> ${request.toAccount}")

        var decision = "APPROVED"
        var reasons = listOf("Safe")
        var aiScore = 0.0

        // --- STEP 1: ASK THE AI SENTINEL (Python) ---
        try {
            val restTemplate = RestTemplate()
            
            val aiPayload = mapOf(
                "fromAccount" to request.fromAccount,
                "toAccount" to request.toAccount,
                "amount" to request.amount,
                "lat" to request.lat,
                "lon" to request.lon
            )

            // Call Python Brain
            val aiResponse = restTemplate.postForObject(
                "http://localhost:5000/analyze", 
                aiPayload, 
                Map::class.java
            )

            decision = aiResponse?.get("decision") as String
            reasons = aiResponse?.get("reasons") as List<String>
            
            // Handle Number conversion safely (JSON can return Integer or Double)
            val scoreRaw = aiResponse?.get("score")
            aiScore = when (scoreRaw) {
                is Double -> scoreRaw
                is Int -> scoreRaw.toDouble()
                else -> 0.0
            }

            println("🤖 AI VERDICT: $decision (Score: $aiScore) | Reasons: $reasons")

            // BLOCK IF AI SAYS SO
            if (decision == "BLOCKED") {
                return ResponseEntity.status(403).body(mapOf(
                    "status" to "blocked",
                    "message" to "❌ Transaction Blocked by AI Sentinel",
                    "reasons" to reasons
                ))
            }

        } catch (e: Exception) {
            println("⚠️ WARNING: AI Engine Offline. Proceeding with default checks. (${e.message})")
        }

        // --- STEP 2: SMART STRATEGY SELECTION MATRIX ---
        
        var strategyMode = "MERKLE" // Default Strategy

        // RULE 1: PRIVACY (ZKP)
        // Demo Logic: If amount is exactly 777, use ZKP
        if (request.amount == 777.0) {
            strategyMode = "ZKP" 
        }
        // RULE 2: SECURITY (Multi-Sig)
        // Logic: If amount > 1,00,000, require Dual Auth
        else if (request.amount > 100000) {
            strategyMode = "MULTISIG"
        }
        // RULE 3: INTEGRITY (TSA)
        // Logic: If AI Risk Score is Moderate (> 0.1), seal with Timestamp
        else if (aiScore > 0.1) {
            strategyMode = "TSA"
        }
        // RULE 4: EFFICIENCY (Merkle)
        // Logic: Safe, small transactions
        else {
            strategyMode = "MERKLE"
        }
        
        println("🧠 INTELLIGENT SWITCH: Amount ₹${request.amount} | Risk $aiScore -> Using $strategyMode")

        // --- STEP 3: SEAL THE TRANSACTION ---
        val event = TransactionEvent(
            id = request.id,
            fromAccount = request.fromAccount,
            toAccount = request.toAccount,
            amount = request.amount,
            timestamp = Instant.now()
        )
        
        // Pass the chosen mode to the Service
        auditService.processTransaction(event, strategyMode)

        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "Payment Sealed via $strategyMode Strategy",
            "transactionId" to request.id,
            "ai_analysis" to reasons,
            "strategy_used" to strategyMode
        ))
    }

    // --- DASHBOARD ENDPOINTS ---

    @GetMapping("/blocks")
    fun getRecentBlocks(): ResponseEntity<List<com.fin.shadow_ledger.model.AuditBlock>> {
        val blocks = auditService.getRecentBlocks() 
        return ResponseEntity.ok(blocks)
    }

    @GetMapping("/verify/{id}")
    fun verifyTransaction(@PathVariable id: Long): ResponseEntity<String> {
        val isValid = auditService.verifyTransaction(id)
        return if (isValid) {
            ResponseEntity.ok("✅ Verified: Transaction $id is secured on the ledger.")
        } else {
            ResponseEntity.status(404).body("❌ Not Found: Transaction $id is not yet audited.")
        }
    }
}