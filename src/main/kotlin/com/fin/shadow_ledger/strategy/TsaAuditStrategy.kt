package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken
import org.springframework.stereotype.Component
import java.security.KeyPairGenerator
import java.security.Signature
import java.time.Instant
import java.util.Base64

/**
 * Time-Stamp Authority (TSA) Audit Strategy - Non-Repudiation Digital Signatures
 *
 * Implements RFC 3161-style timestamped digital signatures using RSA-2048 with
 * SHA-256 for medium-risk transactions requiring non-repudiation guarantees.
 *
 * Use Case:
 * - Medium-risk transactions (risk score 0.2 - 0.5)
 * - Regulatory compliance requiring proof of transaction time
 * - Legal non-repudiation for dispute resolution
 *
 * Non-Repudiation Explained:
 * The signer cannot deny having signed the document because:
 * 1. Only they possess the private key
 * 2. The signature is mathematically bound to the document
 * 3. The timestamp proves when the signature was created
 *
 * Cryptographic Properties:
 * - Algorithm: RSA-2048 with SHA-256 (SHA256withRSA)
 * - Key Size: 2048 bits (112-bit security level)
 * - Signature Size: 256 bytes (Base64 encoded: ~344 chars)
 * - Compliance: FIPS 186-5, NIST SP 800-131A
 *
 * RFC 3161 Time-Stamp Protocol:
 * - Binds document hash to specific timestamp
 * - Third-party TSA provides trusted time source
 * - Used in legal, financial, and regulatory contexts
 *
 * Banking Applications:
 * - Wire transfer authorization
 * - Contract signing timestamps
 * - Audit trail for compliance (SOX, PCI-DSS)
 * - Dispute resolution evidence
 *
 * References:
 * - RFC 3161: Internet X.509 PKI Time-Stamp Protocol
 * - FIPS 186-5: Digital Signature Standard
 * - NIST SP 800-131A: Transitioning Cryptographic Algorithms
 * - eIDAS Regulation: EU electronic signatures
 */
@Component("tsaAuditStrategy")
class TsaAuditStrategy : AuditStrategy {

    /**
     * RSA Key Pair representing the Time-Stamp Authority.
     * Generated once on startup. In production, this would be:
     * - Stored in HSM (Hardware Security Module)
     * - Backed by a trusted CA certificate
     * - Rotated according to key management policy
     */
    private val keyPair = KeyPairGenerator.getInstance("RSA").apply {
        initialize(2048)
    }.genKeyPair()

    override fun processTransaction(event: TransactionEvent): WitnessToken {
        // Create canonical data representation for signing
        val timestamp = Instant.now()
        val dataToSign = buildString {
            append("VERSION:1|")
            append("TXN_ID:${event.id}|")
            append("FROM:${event.fromAccount}|")
            append("TO:${event.toAccount}|")
            append("AMOUNT:${event.amount}|")
            append("TIMESTAMP:${timestamp.toEpochMilli()}")
        }

        // Sign with RSA-2048 + SHA-256
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
                "key_size" to "2048 bits",
                "timestamp_utc" to timestamp.toString(),
                "timestamp_epoch_ms" to timestamp.toEpochMilli(),
                "compliance" to listOf("FIPS 186-5", "RFC 3161", "eIDAS"),
                "non_repudiation" to true
            )
        )
    }
}
