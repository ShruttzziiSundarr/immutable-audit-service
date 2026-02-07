package com.fin.shadow_ledger.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PaymentRequest(
    @field:NotBlank(message = "Transaction ID is mandatory")
    val transactionId: String, // UUID

    @field:NotBlank(message = "Sender ID is mandatory")
    val fromAccount: String,

    @field:NotBlank(message = "Receiver ID is mandatory")
    val toAccount: String,

    @field:Min(value = 1, message = "Amount must be positive")
    val amount: Double,

    @field:NotBlank(message = "Currency is mandatory")
    val currency: String = "INR",

    val timestamp: String, // ISO-8601

    @field:NotBlank(message = "Device ID is mandatory")
    val deviceId: String,

    // Nested Location Object
    val location: LocationData,

    @field:NotBlank(message = "IP Address is mandatory")
    val ipAddress: String,

    // Step 3 Compliance: The Client's Cryptographic Signature
    @field:NotBlank(message = "Client Signature is mandatory for non-repudiation")
    val clientSignature: String
)

data class LocationData(
    val lat: Double,
    val lon: Double
)