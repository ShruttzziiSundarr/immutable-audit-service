package com.fin.shadow_ledger.repository

import com.fin.shadow_ledger.model.TransactionWitness
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionWitnessRepository : JpaRepository<TransactionWitness, Long> {
    fun existsByTransactionId(transactionId: String): Boolean
}