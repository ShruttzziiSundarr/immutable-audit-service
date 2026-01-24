package com.fin.shadow_ledger.service.strategy

import com.fin.shadow_ledger.crypto.MerkleTree
import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.model.AuditBlock
import com.fin.shadow_ledger.model.TransactionWitness
import com.fin.shadow_ledger.repository.AuditBlockRepository
import com.fin.shadow_ledger.repository.TransactionWitnessRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Collections

@Service("MERKLE_STRATEGY") // <--- This name is important for the config switch!
class MerkleAuditStrategy(
    private val blockRepository: AuditBlockRepository,
    private val witnessRepository: TransactionWitnessRepository
) : AuditStrategy {

    private val BATCH_SIZE = 10
    private val transactionBuffer = Collections.synchronizedList(mutableListOf<TransactionEvent>())

    @Transactional
    override fun process(event: TransactionEvent) {
        transactionBuffer.add(event)
        println("[Merkle Strategy] Buffer: ${transactionBuffer.size}/$BATCH_SIZE")

        if (transactionBuffer.size >= BATCH_SIZE) {
            sealBlock()
        }
    }

    private fun sealBlock() {
        println("[Merkle Strategy] Sealing Batch...")
        val currentBatch = transactionBuffer.toList()
        transactionBuffer.clear()

        // 1. Build Tree
        val hashes = currentBatch.map { it.toHash() }
        val merkleTree = MerkleTree(hashes)

        // 2. Link to Previous Block
        val lastBlock = blockRepository.findFirstByOrderByBlockHeightDesc()
        val prevHash = lastBlock?.merkleRoot ?: "00000000000000000000000000000000"
        val newHeight = (lastBlock?.blockHeight ?: 0L) + 1

        // 3. Save Block
        val newBlock = AuditBlock(
            merkleRoot = merkleTree.root,
            previousBlockHash = prevHash,
            transactionCount = currentBatch.size,
            blockHeight = newHeight
        )
        val savedBlock = blockRepository.save(newBlock)

        // 4. Save Receipts
        val witnesses = currentBatch.mapIndexed { index, tx ->
            TransactionWitness(
                transactionId = tx.id,
                transactionHash = tx.toHash(),
                auditBlock = savedBlock,
                merkleProof = merkleTree.generateProof(index).toString()
            )
        }
        witnessRepository.saveAll(witnesses)
        println("[Merkle Strategy] Block Sealed! Root: ${merkleTree.root}")
    }
}