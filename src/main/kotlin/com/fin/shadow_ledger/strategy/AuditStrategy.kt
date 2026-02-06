package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken

/**
 * Audit Strategy Interface - Risk-Adaptive Cryptographic Selection
 *
 * Strategies:
 * 1. MERKLE - Batch Integrity (Low Risk)
 * 2. TSA - Timestamped Signature (Medium Risk)
 * 3. MULTISIG - Dual Approval (High Risk)
 * 4. ZKP - Privacy Commitment (VIP/Secret)
 */
interface AuditStrategy {
    fun processTransaction(event: TransactionEvent): WitnessToken
}