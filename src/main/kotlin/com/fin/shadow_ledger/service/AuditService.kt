package com.fin.shadow_ledger.service

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.service.strategy.AuditStrategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuditService(
    private val strategies: Map<String, AuditStrategy>,
    @Value("\${audit.mode}") private val activeMode: String
) {

    fun processTransaction(event: TransactionEvent) {
        val strategy = strategies[activeMode] 
            ?: throw RuntimeException("Invalid Audit Mode: $activeMode. Available: ${strategies.keys}")
            
        strategy.process(event)
    }

    fun verifyTransaction(id: Long): Boolean {
        val strategy = strategies[activeMode]
            ?: throw RuntimeException("Invalid Audit Mode: $activeMode")
        
        return strategy.verify(id)
    }
}