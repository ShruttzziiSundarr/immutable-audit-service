package com.fin.shadow_ledger.repository  

import com.fin.shadow_ledger.model.AuditBlock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AuditBlockRepository : JpaRepository<AuditBlock, UUID> {
    fun findFirstByOrderByBlockHeightDesc(): AuditBlock?
    // Add this inside the interface
    fun findTop10ByOrderByBlockHeightDesc(): List<AuditBlock>
}