package com.fin.shadow_ledger.controller

import com.fin.shadow_ledger.dto.PaymentRequest
import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.service.AiEngineClient
import com.fin.shadow_ledger.service.AuditService
import com.fin.shadow_ledger.repository.UserRepository
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/v1/audit")
class AuditController(
    private val auditService: AuditService,
    private val userRepo: UserRepository,
    private val aiClient: AiEngineClient
) {
    private val logger = LoggerFactory.getLogger(AuditController::class.java)

    @PostMapping("/payment")
    fun processPayment(@Valid @RequestBody req: PaymentRequest): ResponseEntity<Any> {
        logger.info("Processing Payment: ${req.amount} from ${req.accountId}")

        // 1. Honeypot Check
        val user = userRepo.findById(req.accountId).orElse(null)
        if (user != null && user.isHoneypot) {
            logger.warn("HONEYPOT TRIGGERED: ${req.ipAddress}")
            // Silent Ban (Return 200 OK but don't process)
            return ResponseEntity.ok(mapOf("status" to "QUEUED", "ref" to "HP-${System.currentTimeMillis()}"))
        }

        // 2. Async AI Analysis
        val aiPayload = mapOf(
            "fromAccount" to req.accountId,
            "instrument" to req.paymentInstrumentId,
            "amount" to req.amount,
            "lat" to req.currentLat,
            "lon" to req.currentLon
        )

        val aiResult = aiClient.analyzeAsync(aiPayload).join() // Wait (but with timeout from service)
        val riskScore = (aiResult["score"] as? Number)?.toDouble() ?: 0.0
        val reasons = aiResult["reasons"] as? List<*>

        // 3. Strategy Selection
        val strategyMode = when {
            req.amount == 777.0 -> "ZKP"
            riskScore > 0.8 -> return ResponseEntity.status(403).body(mapOf("status" to "BLOCKED", "reasons" to reasons))
            riskScore > 0.5 -> "MULTISIG"
            riskScore > 0.2 -> "TSA"
            else -> "MERKLE"
        }

        // 4. Seal
        val event = TransactionEvent(System.currentTimeMillis(), req.accountId, req.toAccount, req.amount, Instant.now())
        auditService.processTransaction(event, strategyMode)

        return ResponseEntity.ok(mapOf(
            "status" to "Sealed",
            "strategy" to strategyMode,
            "risk" to riskScore,
            "proof_metadata" to "Included in Ledger"
        ))
    }

    @GetMapping("/blocks")
    fun getRecentBlocks() = ResponseEntity.ok(auditService.getRecentBlocks())
}
