package com.fin.shadow_ledger.controller

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.service.AuditService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.time.Instant

// Helper class to map the incoming JSON from pay.html
data class PaymentRequest(
    val id: Long,
    val fromAccount: String,
    val toAccount: String,
    val amount: Double,
    val lat: Double? = null, 
    val lon: Double? = null  
)

@RestController
@RequestMapping("/api/v1/audit")
class AuditController(private val auditService: AuditService) {

    @PostMapping("/payment")
    fun processPayment(@RequestBody request: PaymentRequest): ResponseEntity<Map<String, Any>> {
        println("💰 Payment Request: ₹${request.amount} | ${request.fromAccount} -> ${request.toAccount}")

        // 1. CALL THE PYTHON TRIDENT AI BRAIN
        try {
            val restTemplate = RestTemplate()
            
            val aiPayload = mapOf(
                "fromAccount" to request.fromAccount,
                "toAccount" to request.toAccount,
                "amount" to request.amount,
                "lat" to request.lat,
                "lon" to request.lon
            )

            val aiResponse = restTemplate.postForObject(
                "http://localhost:5000/analyze", 
                aiPayload, 
                Map::class.java
            )

            val decision = aiResponse?.get("decision") as String
            val reasons = aiResponse?.get("reasons") as List<String>
            val score = aiResponse?.get("score")

            println("🤖 AI VERDICT: $decision (Score: $score) | Reasons: $reasons")

            if (decision == "BLOCKED") {
                return ResponseEntity.status(403).body(mapOf(
                    "status" to "blocked",
                    "message" to "❌ Transaction Blocked by AI Sentinel",
                    "reasons" to reasons
                ))
            }

        } catch (e: Exception) {
            println("⚠️ WARNING: AI Engine is Offline. Proceeding without check. Error: ${e.message}")
        }

        // 2. IF SAFE (OR AI OFFLINE), SEAL IT ON THE LEDGER
        val event = TransactionEvent(
            id = request.id,
            fromAccount = request.fromAccount,
            toAccount = request.toAccount,
            amount = request.amount,
            // FIX: Removed .toString() so it passes the raw Instant object
            timestamp = Instant.now() 
        )
        
        auditService.processTransaction(event)

        // 3. RETURN SUCCESS TO UI
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "Payment Verified, Sealed & Audited on Ledger",
            "transactionId" to request.id
        ))
    }

    // --- OLD ENDPOINTS ---

    @PostMapping("/transaction")
    fun ingestTransaction(@RequestBody event: TransactionEvent): ResponseEntity<String> {
        auditService.processTransaction(event)
        return ResponseEntity.ok("Transaction ${event.id} received and buffered.")
    }

    @GetMapping("/verify/{id}")
    fun verifyTransaction(@PathVariable id: Long): ResponseEntity<String> {
        val isValid = auditService.verifyTransaction(id)
        
        return if (isValid) {
            ResponseEntity.ok("✅ Verified: Transaction $id is immutable and secured on the ledger.")
        } else {
            ResponseEntity.status(404).body("❌ Not Found: Transaction $id is not yet audited.")
        }
    }

    @GetMapping("/blocks")
    fun getRecentBlocks(): ResponseEntity<List<com.fin.shadow_ledger.model.AuditBlock>> {
        val blocks = auditService.getRecentBlocks() 
        return ResponseEntity.ok(blocks)
    }
}