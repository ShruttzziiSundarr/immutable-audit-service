package com.fin.shadow_ledger.controller

import com.fin.shadow_ledger.dto.TransactionEvent
import com.fin.shadow_ledger.service.AuditService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/audit")
class AuditController(private val auditService: AuditService) {

    @PostMapping("/transaction")
    fun ingestTransaction(@RequestBody event: TransactionEvent): ResponseEntity<String> {
        auditService.processTransaction(event)
        return ResponseEntity.ok("Transaction ${event.id} received and buffered.")
    }

    @GetMapping("/verify/{id}")
    fun verifyTransaction(@PathVariable id: Long): ResponseEntity<String> {
        val isValid = auditService.verifyTransaction(id)
        
        return if (isValid) {
            ResponseEntity.ok("✅ Verified: Transaction $id is immutable and secured on the ledger.")
        } else {
            ResponseEntity.status(404).body("❌ Not Found: Transaction $id is not yet audited.")
        }
    }
}