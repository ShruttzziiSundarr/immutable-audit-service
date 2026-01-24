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

@Service("MERKLE_STRATEGY")
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

    @Transactional(readOnly = true)
    override fun verify(transactionId: Long): Boolean {
        // Look for the receipt using our finder method
        val witness = witnessRepository.findByTransactionId(transactionId)

        return if (witness != null) {
            println("[Merkle Strategy] Verified! Transaction $transactionId is in Block ${witness.auditBlock.blockHeight}")
            println("[Merkle Strategy] Merkle Root: ${witness.auditBlock.merkleRoot}")
            true
        } else {
            println("[Merkle Strategy] Failed! Transaction $transactionId not found.")
            false
        }
    }

    private fun sealBlock() {
        println("[Merkle Strategy] Sealing Batch...")
        val currentBatch = transactionBuffer.toList()
        transactionBuffer.clear()

        val hashes = currentBatch.map { it.toHash() }
        val merkleTree = MerkleTree(hashes)

        val lastBlock = blockRepository.findFirstByOrderByBlockHeightDesc()
        val prevHash = lastBlock?.merkleRoot ?: "00000000000000000000000000000000"
        val newHeight = (lastBlock?.blockHeight ?: 0L) + 1

        val newBlock = AuditBlock(
            merkleRoot = merkleTree.root,
            previousBlockHash = prevHash,
            transactionCount = currentBatch.size,
            blockHeight = newHeight
        )
        val savedBlock = blockRepository.save(newBlock)

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