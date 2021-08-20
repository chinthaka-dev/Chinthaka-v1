package com.chinthaka.chinthaka_beta.repositories

import android.util.Log
import com.chinthaka.chinthaka_beta.data.entities.Category
import com.chinthaka.chinthaka_beta.data.entities.CategoryUpdate
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.other.Resource
import com.chinthaka.chinthaka_beta.other.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@ActivityScoped     // @ActivityScoped is used to make sure we always pick the single instance always.
class DefaultCategoryRepository : CategoryRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val users = firestore.collection("users")
    private val categories = firestore.collection("categories")

    override suspend fun getCategories() = withContext(Dispatchers.IO) {
        safeCall {
            // Chunks are important as Firebase limits the results of wherein queries to 10.

            val currentUserId = FirebaseAuth.getInstance().uid!!
            val currentUser =  users.document(currentUserId).get().await().toObject(User::class.java)

            val resultList = categories
                .get()
                .await()
                .toObjects(Category::class.java)
                .onEach { category ->
                    if (currentUser != null) {
                        category.isSelected = category.categoryName in currentUser.selectedInterests
                    }
                }

            Resource.Success(resultList.toList())
        }
    }

    override suspend fun updateInterestsForAUser(categoryUpdate: CategoryUpdate) =
        withContext(Dispatchers.IO) {
            safeCall {
                val map = mutableMapOf(
                    "selectedInterests" to categoryUpdate.updatedInterests
                )

                users.document(categoryUpdate.uidToUpdate).update(map.toMap()).await()
                Resource.Success(Any())
            }
        }

}