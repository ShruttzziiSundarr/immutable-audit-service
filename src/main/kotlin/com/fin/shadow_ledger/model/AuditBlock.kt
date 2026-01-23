package com.fin.shadow_ledger.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audit_blocks")
class AuditBlock(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val merkleRoot: String,        // The fingerprint of the whole batch
    val previousBlockHash: String, // Links to the previous block (Blockchain style)
    val transactionCount: Int,
    val blockHeight: Long,         // Block #1, #2, #3...
    val timestamp: Instant = Instant.now()
)