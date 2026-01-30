package com.fin.shadow_ledger.dto

data class WitnessToken(
    val transactionId: String,
    val witnessToken: String, // The main hash/signature
    val strategyName: String,

    // NEW: Detailed Proofs for auditing
    val metadata: Map<String, Any> = emptyMap()
)
