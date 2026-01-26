package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken
import org.springframework.stereotype.Component

@Component("merkleAuditStrategy")
class MerkleAuditStrategy : AuditStrategy {
    override fun processTransaction(event: TransactionEvent): WitnessToken {
        val leafHash = "LEAF_${event.amount}_${event.toAccount.hashCode()}"
        return WitnessToken(
            transactionId = event.id.toString(),
            witnessToken = "ROOT_${leafHash.hashCode()}",
            strategyName = "MERKLE"
        )
    }
}