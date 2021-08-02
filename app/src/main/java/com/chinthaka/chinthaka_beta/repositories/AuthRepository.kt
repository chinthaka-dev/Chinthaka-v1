package com.chinthaka.chinthaka_beta.repositories

import com.chinthaka.chinthaka_beta.other.Resource
import com.google.firebase.auth.AuthResult

interface AuthRepository {

    suspend fun register(email: String, userName: String, password: String): Resource<AuthResult>

    suspend fun login(email: String, password: String): Resource<AuthResult>
}