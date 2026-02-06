package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken
import org.springframework.stereotype.Component
import java.security.KeyPairGenerator
import java.security.Signature
import java.time.Instant
import java.util.Base64

@Component("tsaAuditStrategy")
class TsaAuditStrategy : AuditStrategy {

    // Generate KeyPair ONCE on startup (Simulating the Authority)
    private val keyPair = KeyPairGenerator.getInstance("RSA").apply {
        initialize(2048)
    }.genKeyPair()

    override fun processTransaction(event: TransactionEvent): WitnessToken {
        val timestamp = Instant.now()
        val dataToSign = "TXN:${event.id}|AMT:${event.amount}|TIME:${timestamp.toEpochMilli()}"

        // Sign with RSA-2048
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(keyPair.private)
        signer.update(dataToSign.toByteArray(Charsets.UTF_8))
        val signatureBytes = signer.sign()
        val signatureB64 = Base64.getEncoder().encodeToString(signatureBytes)

        return WitnessToken(
            transactionId = event.id.toString(),
            witnessToken = signatureB64,
            strategyName = "TSA_RSA_2048",
            metadata = mapOf(
                "algorithm" to "SHA256withRSA",
                "timestamp_epoch" to timestamp.toEpochMilli(),
                "non_repudiation" to true
            )
        )
    }
}