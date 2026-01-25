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

    // Inject ALL 5 Strategies
    @Qualifier("simpleAuditStrategy") private val simpleStrategy: AuditStrategy,
    @Qualifier("merkleAuditStrategy") private val merkleStrategy: AuditStrategy,
    @Qualifier("tsaAuditStrategy") private val tsaStrategy: AuditStrategy,
    @Qualifier("zkpAuditStrategy") private val zkpStrategy: AuditStrategy,
    @Qualifier("multiSigAuditStrategy") private val multiSigStrategy: AuditStrategy
) {

    fun processTransaction(event: TransactionEvent, mode: String = "MERKLE") {
        
        // 1. Select the Cryptographic Engine
        val strategy = when (mode) {
            "TSA" -> tsaStrategy
            "ZKP" -> zkpStrategy
            "MULTISIG" -> multiSigStrategy
            "SIMPLE" -> simpleStrategy
            else -> merkleStrategy
        }

        println("⚙️ EXECUTING STRATEGY: [$mode] for Transaction ID: ${event.id}")

        // 2. Execute the Strategy Logic
        val blockData = strategy.processTransaction(event)

        // 3. Seal to Immutable Ledger (Database)
        // FIX: Removed .toString() from timestamp because Entity expects Instant
        // FIX: Ensure 'blockHeight' is Long
        val block = AuditBlock(
            merkleRoot = blockData.witnessToken, 
            previousBlockHash = "HASH_${event.id.hashCode()}", 
            transactionCount = 1,
            blockHeight = auditBlockRepository.count() + 1,
            timestamp = Instant.now() 
        )
        val savedBlock = auditBlockRepository.save(block)

        // 4. Issue the Receipt (Witness Record)
        // FIX: Changed parameter name from 'block' to 'auditBlock' (Standard JPA naming)
        val witness = TransactionWitness(
            transactionId = event.id.toString(),
            transactionHash = blockData.witnessToken,
            merkleProof = "Secured via $mode Strategy",
            auditBlock = savedBlock 
        )
        witnessRepository.save(witness)
        
        println("✅ BLOCK SEALED: Height ${savedBlock.blockHeight} | Proof: ${witness.transactionHash}")
    }

    // --- READ-ONLY METHODS ---
    fun getRecentBlocks(): List<AuditBlock> = auditBlockRepository.findAll()
    
    fun verifyTransaction(id: Long): Boolean = witnessRepository.existsByTransactionId(id.toString())
}