package com.fin.shadow_ledger.controller

import com.fin.shadow_ledger.dto.PaymentRequest
import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.service.AuditService
import com.fin.shadow_ledger.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.time.Instant

@RestController
@RequestMapping("/api/v1/audit")
class AuditController(
    private val auditService: AuditService,
    private val userRepository: UserRepository // Inject Repo
) {

    @PostMapping("/payment")
    fun processPayment(@RequestBody request: PaymentRequest): ResponseEntity<Any> {
        println("💰 Incoming Tx: ₹${request.amount} from ${request.accountId}")

        // --- NEW: HONEYPOT DEFENSE SYSTEM ---
        val sender = userRepository.findById(request.accountId).orElse(null)

        if (sender != null && sender.isHoneypot) {
            println("🚨 SECURITY ALERT: HONEYPOT TRIGGERED BY IP: ${request.ipAddress}")
            println("🔒 SYSTEM LOCKDOWN INITIATED.")

            // Return 418 (Teapot) or 403 (Forbidden) with a scary message
            return ResponseEntity.status(418).body(mapOf(
                "status" to "CRITICAL_SECURITY_EVENT",
                "error" to "INTRUSION_DETECTED",
                "message" to "This account is monitored. Your IP [${request.ipAddress}] has been logged and reported.",
                "action" to "SYSTEM_LOCKDOWN"
            ))
        }
        // -------------------------------------

        var strategyMode = "MERKLE"
        var riskScore = 0.0
        var reasons = listOf("Safe")

        // Call Python AI (Same as before)
        try {
            val restTemplate = RestTemplate()
            val aiResponse = restTemplate.postForObject(
                "http://localhost:5000/analyze",
                mapOf("fromAccount" to request.accountId, "amount" to request.amount, "lat" to request.currentLat, "lon" to request.currentLon),
                Map::class.java
            )
            val scoreRaw = aiResponse?.get("score")
            riskScore = if (scoreRaw is Int) scoreRaw.toDouble() else (scoreRaw as? Double ?: 0.0)
            reasons = aiResponse?.get("reasons") as List<String>
        } catch (e: Exception) {
            println("⚠️ AI Offline.")
        }

        // Logic (Same as before)
        if (riskScore > 0.8) return ResponseEntity.status(403).body(mapOf("status" to "blocked", "reasons" to reasons))
        if (request.amount == 777.0) strategyMode = "ZKP"
        else if (riskScore > 0.5) strategyMode = "MULTISIG"
        else if (riskScore > 0.2) strategyMode = "TSA"

        // Seal
        val event = TransactionEvent(System.currentTimeMillis(), request.accountId, request.toAccount, request.amount, Instant.now())
        auditService.processTransaction(event, strategyMode)

        return ResponseEntity.ok(mapOf("status" to "success", "strategy" to strategyMode, "risk_score" to riskScore))
    }

    @GetMapping("/blocks")
    fun getRecentBlocks() = ResponseEntity.ok(auditService.getRecentBlocks())
}
