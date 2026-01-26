package com.fin.shadow_ledger.model

import jakarta.persistence.*

@Entity
@Table(name = "transaction_witnesses")
data class TransactionWitness(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val transactionId: String,

    @Column(nullable = false, length = 1000)
    val transactionHash: String, // The specific proof (ZKP, TSA, etc.)

    @Column(nullable = false)
    val merkleProof: String, // Human readable status like "Secured via TSA"

    @ManyToOne
    @JoinColumn(name = "block_height")
    val auditBlock: AuditBlock // Links this receipt to the Block
)