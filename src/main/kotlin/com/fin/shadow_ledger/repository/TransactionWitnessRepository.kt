package com.fin.shadow_ledger.repository

import com.fin.shadow_ledger.model.TransactionWitness
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionWitnessRepository : JpaRepository<TransactionWitness, Long> {
    
    // 1. Critical: Used by AuditService.verifyTransaction()
    // We use String because we stored the ID as "event.id.toString()"
    fun existsByTransactionId(transactionId: String): Boolean

    // 2. Optional: Used if you want to fetch the full receipt
    fun findByTransactionId(transactionId: String): TransactionWitness?
}