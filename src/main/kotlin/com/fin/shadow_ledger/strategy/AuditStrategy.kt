package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken

interface AuditStrategy {
    fun processTransaction(event: TransactionEvent): WitnessToken
}
