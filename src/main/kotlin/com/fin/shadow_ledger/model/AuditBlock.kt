package com.fin.shadow_ledger.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "audit_blocks")
data class AuditBlock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val blockHeight: Long = 0,

    @Column(nullable = false)
    val merkleRoot: String, // The final hash proof

    @Column(nullable = false)
    val previousBlockHash: String, // Links to previous block (Blockchain logic)

    @Column(nullable = false)
    val transactionCount: Int,

    @Column(nullable = false)
    val timestamp: Instant = Instant.now()
)