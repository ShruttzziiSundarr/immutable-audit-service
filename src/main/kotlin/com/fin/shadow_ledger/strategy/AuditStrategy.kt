package com.fin.shadow_ledger.service.strategy

import com.fin.shadow_ledger.dto.TransactionEvent

interface AuditStrategy {
    fun process(event: TransactionEvent)
}