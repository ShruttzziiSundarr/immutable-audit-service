package com.fin.shadow_ledger.model

import jakarta.persistence.*

@Entity
@Table(name = "transaction_witnesses")
class TransactionWitness(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val transactionId: String,  // Changed to String to match AuditService usage
    val transactionHash: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    val auditBlock: AuditBlock,

    @Column(columnDefinition = "text") // Stores the proof as a long string
    val merkleProof: String
)