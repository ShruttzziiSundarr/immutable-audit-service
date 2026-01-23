package com.fin.shadow_ledger.repository  // <--- Check this line again

import com.fin.shadow_ledger.model.TransactionWitness
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TransactionWitnessRepository : JpaRepository<TransactionWitness, UUID>