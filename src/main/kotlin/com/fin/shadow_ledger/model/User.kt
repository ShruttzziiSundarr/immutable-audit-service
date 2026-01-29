package com.fin.shadow_ledger.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(nullable = false, unique = true)
    val accountId: String,

    @Column(nullable = false)
    val fullName: String,

    @Column(nullable = false)
    val kycTier: String,

    val homeLatitude: Double,
    val homeLongitude: Double,

    // NEW: The Trap Field
    @Column(nullable = false)
    val isHoneypot: Boolean = false,

    val accountCreatedDate: Instant = Instant.now()
)
