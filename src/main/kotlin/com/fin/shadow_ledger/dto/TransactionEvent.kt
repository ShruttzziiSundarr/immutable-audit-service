package com.fin.shadow_ledger.dto  // <--- This was likely missing or wrong

import java.time.Instant
import com.fin.shadow_ledger.crypto.HashUtils

data class TransactionEvent(
    val id: Long,
    val fromAccount: String,
    val toAccount: String,
    val amount: Double,
    val timestamp: Instant
) {
    fun toHash(): String {
        val rawData = "$id$fromAccount$toAccount$amount$timestamp"
        return HashUtils.sha256(rawData)
    }
}