package com.fin.shadow_ledger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShadowLedgerApplication

fun main(args: Array<String>) {
    runApplication<ShadowLedgerApplication>(*args)
}