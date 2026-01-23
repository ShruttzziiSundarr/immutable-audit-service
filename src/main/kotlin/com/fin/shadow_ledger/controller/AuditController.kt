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
        return ResponseEntity.ok("Verification logic coming soon for ID: $id")
    }
}