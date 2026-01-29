package com.fin.shadow_ledger.dto

data class PaymentRequest(
    val accountId: String,
    val toAccount: String,
    val amount: Double,
    val currentLat: Double,
    val currentLon: Double,
    val ipAddress: String
)
