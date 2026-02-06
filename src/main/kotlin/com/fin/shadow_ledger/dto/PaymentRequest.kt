package com.fin.shadow_ledger.dto

import jakarta.validation.constraints.*

data class PaymentRequest(
    @field:NotBlank(message = "Account ID is required")
    val accountId: String,

    @field:NotBlank(message = "Instrument ID is required")
    val paymentInstrumentId: String = "DEFAULT",

    @field:NotBlank(message = "Recipient is required")
    val toAccount: String,

    @field:Positive(message = "Amount must be greater than zero")
    @field:Max(value = 100000000, message = "Amount exceeds transaction limit")
    val amount: Double,

    @field:DecimalMin("-90.0") @field:DecimalMax("90.0")
    val currentLat: Double,

    @field:DecimalMin("-180.0") @field:DecimalMax("180.0")
    val currentLon: Double,

    @field:NotBlank
    val ipAddress: String
)