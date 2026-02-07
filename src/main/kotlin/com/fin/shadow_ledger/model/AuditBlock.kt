package com.fin.shadow_ledger.model

import jakarta.persistence.*
import java.time.Instant

@Entity
data class AuditBlock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val transactionId: String, // UUID for tracking

    @Column(length = 1024)
    val clientSignature: String, // Step 3: Non-repudiation proof

    @Column(length = 1024)
    val serverSignature: String, // Step 6: Bank's approval proof

    val riskScore: Double,
    
    val riskFlags: String, // "VELOCITY_SPIKE, NEW_DEVICE"

    val policyVersion: String, // "v1.3" - Crucial for auditing "Why did we approve this?"

    val timestamp: Instant
)