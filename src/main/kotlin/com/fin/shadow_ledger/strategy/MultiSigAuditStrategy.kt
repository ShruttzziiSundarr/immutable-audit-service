package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken
import org.springframework.stereotype.Component

@Component("multiSigAuditStrategy")
class MultiSigAuditStrategy : AuditStrategy {
    override fun processTransaction(event: TransactionEvent): WitnessToken {
        val combinedHash = "MSIG::SIG1(${event.hashCode()})+SIG2(ADMIN_AUTO_APPROVE)"
        return WitnessToken(
            transactionId = event.id.toString(),
            witnessToken = combinedHash,
            strategyName = "MULTISIG"
        )
    }
}