package com.fin.shadow_ledger.controller

import com.fin.shadow_ledger.dto.PaymentRequest
import com.fin.shadow_ledger.service.AiEngineClient
import com.fin.shadow_ledger.service.AuditService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/compliance")
class AuditController(
    private val auditService: AuditService,
    private val aiClient: AiEngineClient
) {
    private val logger = LoggerFactory.getLogger(AuditController::class.java)

    @PostMapping("/transaction")
    fun processTransaction(@RequestBody req: PaymentRequest): ResponseEntity<Any> {
        
        // --- STEP 1: API GATEWAY ---
        if (req.clientSignature.length < 10) { 
            return ResponseEntity.status(400).body(mapOf("error" to "Invalid Signature Format")) 
        }

        // FIX: Changed 'val' to 'var' so we can modify it below
        var riskScore: Double
        var riskFlags: List<String>

        // --- STEP 2: DEMO BYPASS LOGIC ---
        // Force Green for small amounts (< 1000)
        if (req.amount < 1000) {
            logger.info("⚠️ DEMO MODE: Bypassing AI for small amount: ${req.amount}")
            riskScore = 0.05 
            riskFlags = listOf("DEMO_SAFE_MODE", "IDENTITY_VERIFIED")
        } else {
            // Ask AI for large amounts
            try {
                val aiPayload = mapOf(
                    "fromAccount" to req.fromAccount, 
                    "toAccount" to req.toAccount,     
                    "amount" to req.amount,
                    "lat" to req.location.lat,
                    "lon" to req.location.lon,
                    "ip" to req.ipAddress
                )
                
                val aiResult = aiClient.analyzeAsync(aiPayload).join()
                
                riskScore = (aiResult["score"] as? Number)?.toDouble() ?: 0.99
                riskFlags = (aiResult["reasons"] as? List<String>) ?: emptyList()

            } catch (e: Exception) {
                logger.error("❌ AI ERROR: ${e.message}")
                riskScore = 0.99 
                riskFlags = listOf("AI_SERVICE_UNAVAILABLE")
            }
        }

        // --- STEP 3: POLICY ENGINE ---
        val policyAction = evaluatePolicy(riskScore, req.amount)

        logger.info("TX: ${req.transactionId} | Score: $riskScore | Policy: ${policyAction.name}")

        // --- STEP 4: EXECUTION ---
        return when (policyAction) {
            PolicyAction.BLOCK -> {
                ResponseEntity.status(403).body(mapOf(
                    "status" to "BLOCKED",
                    "reason" to "Policy Violation: High Risk Score ($riskScore)",
                    "flags" to riskFlags
                ))
            }
            PolicyAction.STEP_UP_AUTH -> {
                 ResponseEntity.status(402).body(mapOf(
                    "status" to "CHALLENGE_REQUIRED",
                    "challenge" to "MULTISIG_REQUIRED",
                    "risk_score" to riskScore
                ))
            }
            PolicyAction.SETTLE -> {
                val receipt = auditService.commitToLedger(req, riskScore, riskFlags, "ECDSA_P256")
                ResponseEntity.ok(receipt)
            }
        }
    }

    enum class PolicyAction { BLOCK, STEP_UP_AUTH, SETTLE }

    private fun evaluatePolicy(score: Double, amount: Double): PolicyAction {
        return when {
            score > 0.8 -> PolicyAction.BLOCK
            score > 0.6 -> PolicyAction.STEP_UP_AUTH
            amount > 1_000_000 -> PolicyAction.STEP_UP_AUTH
            else -> PolicyAction.SETTLE
        }
    }
}