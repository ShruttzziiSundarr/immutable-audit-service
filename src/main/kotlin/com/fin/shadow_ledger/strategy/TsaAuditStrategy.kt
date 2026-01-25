package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken
import org.springframework.stereotype.Component
import java.time.Instant

@Component("tsaAuditStrategy")
class TsaAuditStrategy : AuditStrategy {
    override fun processTransaction(event: TransactionEvent): WitnessToken {
        val signature = "TSA_SIGNED_${Instant.now().toEpochMilli()}_${event.id}"
        return WitnessToken(
            transactionId = event.id.toString(),
            witnessToken = signature,
            strategyName = "TSA"
        )
    }
}
