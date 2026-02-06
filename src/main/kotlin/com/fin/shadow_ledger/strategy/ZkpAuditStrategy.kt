package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

@Component("zkpAuditStrategy")
class ZkpAuditStrategy : AuditStrategy {

    private val secureRandom = SecureRandom()

    override fun processTransaction(event: TransactionEvent): WitnessToken {
        // Generate random blinding factor
        val blindingFactor = ByteArray(32)
        secureRandom.nextBytes(blindingFactor)
        val blindingHex = Base64.getEncoder().encodeToString(blindingFactor)

        // Commitment = SHA256(amount || blinding_factor)
        val input = "${event.amount}||$blindingHex"
        val digest = MessageDigest.getInstance("SHA-256")
        val commitment = Base64.getEncoder().encodeToString(digest.digest(input.toByteArray()))

        return WitnessToken(
            transactionId = event.id.toString(),
            witnessToken = commitment,
            strategyName = "ZKP_PEDERSEN_COMMITMENT",
            metadata = mapOf(
                "type" to "Hash Commitment",
                "amount_hidden" to true
            )
        )
    }
}