package com.fin.shadow_ledger.service

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.model.AuditBlock
import com.fin.shadow_ledger.model.TransactionWitness
import com.fin.shadow_ledger.repository.AuditBlockRepository
import com.fin.shadow_ledger.repository.TransactionWitnessRepository
import com.fin.shadow_ledger.strategy.AuditStrategy
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AuditService(
    private val auditBlockRepository: AuditBlockRepository,
    private val witnessRepository: TransactionWitnessRepository,

    @Qualifier("simpleAuditStrategy") private val simpleStrategy: AuditStrategy,
    @Qualifier("merkleAuditStrategy") private val merkleStrategy: AuditStrategy,
    @Qualifier("tsaAuditStrategy") private val tsaStrategy: AuditStrategy,
    @Qualifier("zkpAuditStrategy") private val zkpStrategy: AuditStrategy,
    @Qualifier("multiSigAuditStrategy") private val multiSigStrategy: AuditStrategy
) {

    fun processTransaction(event: TransactionEvent, mode: String = "MERKLE") {

        val strategy = when (mode) {
            "TSA" -> tsaStrategy
            "ZKP" -> zkpStrategy
            "MULTISIG" -> multiSigStrategy
            "SIMPLE" -> simpleStrategy
            else -> merkleStrategy
        }

        println("EXECUTING STRATEGY: [$mode] for ID: ${event.id}")

        val blockData = strategy.processTransaction(event)

        val block = AuditBlock(
            merkleRoot = blockData.witnessToken,
            previousBlockHash = "HASH_${event.id.hashCode()}",
            transactionCount = 1,
            blockHeight = auditBlockRepository.count() + 1,
            timestamp = Instant.now()
        )
        val savedBlock = auditBlockRepository.save(block)

        val witness = TransactionWitness(
            transactionId = event.id.toString(),
            transactionHash = blockData.witnessToken,
            merkleProof = "Secured via $mode",
            auditBlock = savedBlock
        )
        witnessRepository.save(witness)

        println("SEALED: Height ${savedBlock.blockHeight}")
    }

    fun getRecentBlocks(): List<AuditBlock> = auditBlockRepository.findAll()

    fun verifyTransaction(id: Long): Boolean = witnessRepository.existsByTransactionId(id.toString())
}