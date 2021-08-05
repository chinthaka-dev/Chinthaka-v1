package com.chinthaka.chinthaka_beta.repositories

import com.chinthaka.chinthaka_beta.other.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    suspend fun register(email: String, userName: String, password: String): Resource<AuthResult>

    suspend fun login(email: String, password: String): Resource<AuthResult>

    suspend fun addNewUserViaSocialLogin(user: FirebaseUser): Resource<FirebaseUser>
}