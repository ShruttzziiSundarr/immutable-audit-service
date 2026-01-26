package com.fin.shadow_ledger.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users") // "user" is a reserved keyword in Postgres, so use "users"
data class User(
    @Id
    @Column(nullable = false, unique = true)
    val accountId: String, // e.g., "ACC-101-VIP"

    @Column(nullable = false)
    val fullName: String,

    @Column(nullable = false)
    val kycTier: String, // "TIER_1", "TIER_2", "VIP"

    // AI Baseline Data
    val homeLatitude: Double,
    val homeLongitude: Double,
    
    var currentTrustScore: Double = 1.0, // Starts perfect, drops if suspicious
    var avgMonthlySpend: Double = 0.0,
    
    val accountCreatedDate: Instant = Instant.now()
)