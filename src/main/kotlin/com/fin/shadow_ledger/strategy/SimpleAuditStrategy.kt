package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken
import org.springframework.stereotype.Component

@Component("simpleAuditStrategy")
class SimpleAuditStrategy : AuditStrategy {
    override fun processTransaction(event: TransactionEvent): WitnessToken {
        return WitnessToken(
            transactionId = event.id.toString(),
            witnessToken = "SIMPLE_HASH_${event.hashCode()}",
            strategyName = "SIMPLE"
        )
    }
}
