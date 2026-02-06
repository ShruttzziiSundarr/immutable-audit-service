package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64

@Component("multiSigAuditStrategy")
class MultiSigAuditStrategy : AuditStrategy {

    // Generate 3 simulated signers
    private val signers: List<Pair<String, KeyPair>> = listOf(
        generateECKeyPair("USER"),
        generateECKeyPair("SYSTEM"),
        generateECKeyPair("COMPLIANCE")
    )

    private fun generateECKeyPair(id: String): Pair<String, KeyPair> {
        val keyGen = KeyPairGenerator.getInstance("EC")
        keyGen.initialize(ECGenParameterSpec("secp256r1"))
        return id to keyGen.generateKeyPair()
    }

    override fun processTransaction(event: TransactionEvent): WitnessToken {
        val dataToSign = "${event.id}|${event.amount}|${event.timestamp}"
        val signatures = mutableListOf<String>()

        // Collect 2 signatures (Threshold)
        for (i in 0 until 2) {
            val (signerId, keyPair) = signers[i]
            val sig = Signature.getInstance("SHA256withECDSA")
            sig.initSign(keyPair.private)
            sig.update(dataToSign.toByteArray())
            val signatureBytes = sig.sign()
            signatures.add("$signerId:${Base64.getEncoder().encodeToString(signatureBytes)}")
        }

        return WitnessToken(
            transactionId = event.id.toString(),
            witnessToken = signatures.joinToString("|"),
            strategyName = "MULTISIG_ECDSA_2OF3",
            metadata = mapOf(
                "threshold" to "2-of-3",
                "curve" to "secp256r1"
            )
        )
    }
}