package com.fin.shadow_ledger.model

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "transaction_witnesses")
class TransactionWitness(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    val transactionId: Long,
    val transactionHash: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    val auditBlock: AuditBlock,

    @Column(columnDefinition = "text") // Stores the proof as a long string
    val merkleProof: String
)