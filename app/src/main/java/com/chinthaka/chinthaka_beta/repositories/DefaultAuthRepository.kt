package com.chinthaka.chinthaka_beta.repositories

import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.other.Resource
import com.chinthaka.chinthaka_beta.other.safeCall
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DefaultAuthRepository : AuthRepository {

    val auth = FirebaseAuth.getInstance()
    val users = FirebaseFirestore.getInstance().collection("users")

    override suspend fun login(email: String, password: String): Resource<AuthResult> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                Resource.Success(result)
            }
        }
    }

    override suspend fun register(
        email: String,
        userName: String,
        password: String
    ): Resource<AuthResult> {

        return withContext(Dispatchers.IO) {
            safeCall {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid!!
                val user = User(userId, userName)
                users.document(userId).set(user).await()
                Resource.Success(result)
            }
        }
    }

    override suspend fun addNewUserViaSocialLogin(
        user: FirebaseUser
    ): Resource<FirebaseUser> {

        return withContext(Dispatchers.IO) {
            safeCall {
                val userId = user.uid
                val displayName = user.displayName
                val userToBeDocumented = User(userId, displayName!!)
                users.document(userId).set(userToBeDocumented).await()
                Resource.Success(user)
            }
        }
    }
}