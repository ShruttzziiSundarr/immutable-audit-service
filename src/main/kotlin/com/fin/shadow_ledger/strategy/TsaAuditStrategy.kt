package com.fin.shadow_ledger.service.strategy

import com.fin.shadow_ledger.crypto.HashUtils
import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.model.AuditBlock
import com.fin.shadow_ledger.model.TransactionWitness
import com.fin.shadow_ledger.repository.AuditBlockRepository
import com.fin.shadow_ledger.repository.TransactionWitnessRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service("TSA_STRATEGY")
class TsaAuditStrategy(
    private val blockRepository: AuditBlockRepository,
    private val witnessRepository: TransactionWitnessRepository
) : AuditStrategy {

    @Transactional
    override fun process(event: TransactionEvent) {
        // 1. Get Trusted Timestamp (Kotlin native)
        val timestamp = Instant.now().toString()
        
        // 2. Create the Payload: Hash + Time + ID
        val payload = "${event.toHash()}:$timestamp:${event.id}"
        
        // 3. Sign it (Using our existing HashUtils for SHA-256)
        val signature = HashUtils.sha256(payload)
        
        // 4. Create the "Token" string
        val token = "TSA-KOTLIN-${signature.take(16)}"

        println("[TSA-Kotlin] ⚡ Signed Transaction ${event.id} | Token: $token")

        // 5. Save to DB
        saveToDatabase(event, token, signature)
    }

    private fun saveToDatabase(event: TransactionEvent, token: String, signature: String) {
        val lastBlock = blockRepository.findFirstByOrderByBlockHeightDesc()
        val prevHash = lastBlock?.merkleRoot ?: "GENESIS_TSA_KOTLIN"
        val newHeight = (lastBlock?.blockHeight ?: 0L) + 1

        val newBlock = AuditBlock(
            merkleRoot = signature,
            previousBlockHash = prevHash,
            transactionCount = 1,
            blockHeight = newHeight
        )
        val savedBlock = blockRepository.save(newBlock)

        val witness = TransactionWitness(
            transactionId = event.id,
            transactionHash = event.toHash(),
            auditBlock = savedBlock,
            merkleProof = token
        )
        witnessRepository.save(witness)
    }

    @Transactional(readOnly = true)
    override fun verify(transactionId: Long): Boolean {
        val witness = witnessRepository.findByTransactionId(transactionId)
        return if (witness != null) {
            println("[TSA-Kotlin] Verifying Token: ${witness.merkleProof}")
            witness.merkleProof.startsWith("TSA-KOTLIN-")
        } else {
            false
        }
    }
}