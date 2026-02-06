package com.fin.shadow_ledger.strategy

import com.fin.shadow_ledger.crypto.HashUtils
import com.fin.shadow_ledger.crypto.MerkleTree
import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.dto.WitnessToken
import org.springframework.stereotype.Component

@Component("merkleAuditStrategy")
class MerkleAuditStrategy : AuditStrategy {

    override fun processTransaction(event: TransactionEvent): WitnessToken {
        // Create transaction data string and hash it
        val txData = "${event.id}:${event.fromAccount}:${event.toAccount}:${event.amount}:${event.timestamp}"
        val txHash = HashUtils.sha256(txData)

        // Simulate a previous block hash to create a tree
        val previousBlockHash = HashUtils.sha256("GENESIS_BLOCK_${System.currentTimeMillis() / 10000}")

        // Build Merkle Tree using your crypto utility
        val tree = MerkleTree(listOf(txHash, previousBlockHash))

        return WitnessToken(
            transactionId = event.id.toString(),
            witnessToken = tree.root,
            strategyName = "MERKLE_SHA256",
            metadata = mapOf(
                "algorithm" to "SHA-256",
                "tree_depth" to 1,
                "verification" to "O(log n)"
            )
        )
    }
}