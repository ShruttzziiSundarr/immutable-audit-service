package com.fin.shadow_ledger.service.strategy

import com.fin.shadow_ledger.dto.TransactionEvent

// Every audit method must be able to do these two things
interface AuditStrategy {
    fun process(event: TransactionEvent)
    fun verify(transactionId: Long): Boolean
}