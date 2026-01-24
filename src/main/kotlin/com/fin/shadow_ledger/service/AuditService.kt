package com.fin.shadow_ledger.service

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.service.strategy.AuditStrategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuditService(
    // Spring collects all classes that implement AuditStrategy into this map
    private val strategies: Map<String, AuditStrategy>,
    
    // We read this value from application.yml
    @Value("\${audit.mode}") private val activeMode: String
) {

    fun processTransaction(event: TransactionEvent) {
        // 1. Pick the strategy based on the config name
        val strategy = strategies[activeMode] 
            ?: throw RuntimeException("Invalid Audit Mode: $activeMode. Available: ${strategies.keys}")
            
        // 2. Let the strategy do the work
        strategy.process(event)
    }
}