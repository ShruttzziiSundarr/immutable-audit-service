package com.fin.shadow_ledger.service.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import org.springframework.stereotype.Service

@Service("TSA_STRATEGY")
class TsaAuditStrategy : AuditStrategy {
    override fun process(event: TransactionEvent) {
        // Logic will go here later
    }

    override fun verify(transactionId: Long): Boolean {
        return true
    }
}