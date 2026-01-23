package com.fin.shadow_ledger.service

import com.fin.shadow_ledger.crypto.MerkleTree
// These imports should now work because we fixed the files above
import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.model.AuditBlock
import com.fin.shadow_ledger.model.TransactionWitness
import com.fin.shadow_ledger.repository.AuditBlockRepository
import com.fin.shadow_ledger.repository.TransactionWitnessRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Collections

@Service
class AuditService(
    private val blockRepository: AuditBlockRepository,
    private val witnessRepository: TransactionWitnessRepository
) {
    private val BATCH_SIZE = 10
    private val transactionBuffer = Collections.synchronizedList(mutableListOf<TransactionEvent>())

    @Transactional
    fun processTransaction(event: TransactionEvent) {
        transactionBuffer.add(event)
        println("Received Transaction: ${event.id}. Buffer size: ${transactionBuffer.size}")

        if (transactionBuffer.size >= BATCH_SIZE) {
            sealBlock()
        }
    }

    private fun sealBlock() {
        println("Sealing Block...")
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
        println("Block Sealed! Merkle Root: ${merkleTree.root}")
    }
}