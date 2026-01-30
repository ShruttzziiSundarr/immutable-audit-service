package com.fin.shadow_ledger.util

import java.security.MessageDigest

class MerkleTree(transactions: List<String>) {
    val root: String
    private val leaves: List<String>

    init {
        leaves = transactions.map { hash(it) }
        root = buildRoot(leaves)
    }

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun buildRoot(hashes: List<String>): String {
        if (hashes.isEmpty()) return ""
        if (hashes.size == 1) return hashes[0]

        val nextLevel = mutableListOf<String>()
        for (i in hashes.indices step 2) {
            val left = hashes[i]
            val right = if (i + 1 < hashes.size) hashes[i + 1] else left // Duplicate last if odd
            nextLevel.add(hash(left + right))
        }
        return buildRoot(nextLevel)
    }
}
