package com.fin.shadow_ledger.crypto

import java.security.MessageDigest

object HashUtils {
    fun sha256(data: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun combineAndHash(left: String, right: String): String {
        val combined = if (left < right) left + right else right + left
        return sha256(combined)
    }
}