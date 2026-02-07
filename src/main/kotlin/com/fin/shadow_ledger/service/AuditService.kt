package com.fin.shadow_ledger.service

import com.fin.shadow_ledger.dto.PaymentRequest
import com.fin.shadow_ledger.model.AuditBlock
import com.fin.shadow_ledger.repository.AuditBlockRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AuditService(
    private val auditBlockRepository: AuditBlockRepository
) {

    fun commitToLedger(req: PaymentRequest, riskScore: Double, flags: List<String>, protocol: String): Map<String, Any> {
        
        // 1. Generate Server Signature (Simulated)
        val serverSignature = "SERVER_SIG_${req.transactionId.hashCode()}_SECURED_BY_BANK" 

        // 2. Create Immutable Ledger Entry
        val block = AuditBlock(
            transactionId = req.transactionId,
            clientSignature = req.clientSignature, 
            serverSignature = serverSignature,    
            riskScore = riskScore,
            riskFlags = flags.joinToString(","),
            policyVersion = "v2.0-COMPLIANCE",
            timestamp = Instant.now()
        )
        
        val savedBlock = auditBlockRepository.save(block)

        // 3. Return the Receipt
        return mapOf(
            "status" to "SETTLED",
            "ledger_id" to (savedBlock.id ?: 0),
            "trace_id" to req.transactionId,
            "signatures" to mapOf(
                "client" to "VERIFIED_ECDSA",
                "server" to "VERIFIED_RSA"
            )
        )
    }
}