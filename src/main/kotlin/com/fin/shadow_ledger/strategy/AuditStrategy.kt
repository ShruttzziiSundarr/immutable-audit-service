package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken

/**
 * Audit Strategy Interface - Risk-Adaptive Cryptographic Selection
 *
 * This interface defines the contract for different cryptographic audit strategies
 * used in the Shadow Ledger system. Each strategy provides different levels of
 * security guarantees based on transaction risk assessment.
 *
 * Strategy Pattern Implementation:
 * - Allows dynamic selection of cryptographic algorithms at runtime
 * - Supports Risk-Based Authentication (RBA) as per NIST SP 800-63B
 * - Enables compliance with PSD2 Strong Customer Authentication requirements
 *
 * Available Strategies:
 * 1. MERKLE - Merkle Tree Integrity Verification (Low Risk)
 *    - O(log n) verification complexity
 *    - Batch-efficient for high-volume, low-value transactions
 *
 * 2. TSA - Timestamped Digital Signature Authority (Medium Risk)
 *    - RSA-2048 with SHA256 (FIPS 186-5 compliant)
 *    - Provides non-repudiation for regulatory compliance
 *
 * 3. MULTISIG - Multi-Signature Threshold Scheme (High Risk)
 *    - 2-of-3 ECDSA threshold signatures
 *    - Requires multiple independent approvals
 *
 * 4. ZKP - Zero-Knowledge Proof Commitment (Privacy-Sensitive)
 *    - Pedersen-style hash commitment
 *    - Hides transaction amount while proving validity
 *
 * References:
 * - NIST SP 800-63B: Digital Identity Guidelines
 * - FIPS 186-5: Digital Signature Standard
 * - RFC 3161: Time-Stamp Protocol
 */
interface AuditStrategy {
    /**
     * Process a transaction and generate a cryptographic witness token.
     *
     * @param event The transaction event containing payment details
     * @return WitnessToken containing cryptographic proof of the transaction
     */
    fun processTransaction(event: TransactionEvent): WitnessToken
}
