package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken
import org.springframework.stereotype.Component

@Component("multiSigAuditStrategy")
class MultiSigAuditStrategy : AuditStrategy {
    override fun processTransaction(event: TransactionEvent): WitnessToken {
        println("🔐 MULTI-SIG MODE: Requesting Dual Authorization...")

        // Step 1: User Signature (Simulated by existing hash)
        val userSig = event.hashCode().toString()
        
        // Step 2: System/Admin Signature (The "Second Key")
        // In real life, this waits for an API approval. Here, we auto-sign if valid.
        val adminSig = "ADMIN_APPROVED_${System.currentTimeMillis()}"

        // Combine them into a Multi-Sig Block
        val combinedHash = "SIG1($userSig)+SIG2($adminSig)"

        return WitnessToken(
            transactionId = event.id.toString(),
            witnessToken = "MSIG::$combinedHash",
            strategyName = "MULTI_SIG"
        )
    }
}