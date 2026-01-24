package com.fin.shadow_ledger.repository

import com.fin.shadow_ledger.model.TransactionWitness
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TransactionWitnessRepository : JpaRepository<TransactionWitness, UUID> {
    // We added this line so we can find receipts by the Transaction ID (e.g., "5")
    fun findByTransactionId(transactionId: Long): TransactionWitness?
}