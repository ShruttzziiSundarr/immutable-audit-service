package com.fin.shadow_ledger.repository

import com.fin.shadow_ledger.model.AuditBlock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuditBlockRepository : JpaRepository<AuditBlock, Long>