package com.fin.shadow_ledger.config

import com.fin.shadow_ledger.model.User
import com.fin.shadow_ledger.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataLoader {

    @Bean
    fun initDatabase(userRepository: UserRepository) = CommandLineRunner {
        if (userRepository.count() == 0L) {
            val users = listOf(
                // Normal Users
                User("ACC-MUM-001", "Rahul Sharma", "TIER_2", 19.0760, 72.8777),
                User("ACC-BLR-VIP", "Priya Tech", "VIP", 12.9716, 77.5946),

                // NEW: The Honeypot Trap
                User(
                    accountId = "ACC-ROOT-ADMIN",
                    fullName = "System Administrator",
                    kycTier = "VIP",
                    homeLatitude = 0.0,
                    homeLongitude = 0.0,
                    isHoneypot = true // <--- Set to TRUE
                )
            )
            userRepository.saveAll(users)
            println("✅ DATABASE SEEDED: Includes Honeypot [ACC-ROOT-ADMIN]")
        }
    }
}
