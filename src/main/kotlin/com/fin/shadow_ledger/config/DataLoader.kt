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
                // 1. The Regular User (Lives in Mumbai)
                User(
                    accountId = "ACC-MUM-001",
                    fullName = "Rahul Sharma",
                    kycTier = "TIER_2",
                    homeLatitude = 19.0760, // Mumbai
                    homeLongitude = 72.8777,
                    avgMonthlySpend = 25000.0
                ),
                // 2. The VIP User (Lives in Bangalore)
                User(
                    accountId = "ACC-BLR-VIP",
                    fullName = "Priya Tech",
                    kycTier = "VIP",
                    homeLatitude = 12.9716, // Bangalore
                    homeLongitude = 77.5946,
                    avgMonthlySpend = 500000.0
                )
            )
            userRepository.saveAll(users)
            println("✅ DUMMY USERS LOADED: Rahul (Mumbai) & Priya (Bangalore)")
        }
    }
}