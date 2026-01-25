package com.fin.shadow_ledger.dto

data class WitnessToken(
    val transactionId: String,
    val witnessToken: String, // The Hash or Proof
    val strategyName: String  // "TSA", "MERKLE", "ZKP", etc.
)