package com.fin.shadow_ledger.repository

import com.fin.shadow_ledger.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String>