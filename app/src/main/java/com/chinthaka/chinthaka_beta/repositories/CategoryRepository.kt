package com.chinthaka.chinthaka_beta.repositories

import com.chinthaka.chinthaka_beta.data.entities.Category
import com.chinthaka.chinthaka_beta.data.entities.CategoryUpdate
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.other.Resource


interface CategoryRepository {

    suspend fun getCategories(): Resource<List<Category>>

    suspend fun updateInterestsForAUser(categoryUpdate: CategoryUpdate): Resource<Any>
}