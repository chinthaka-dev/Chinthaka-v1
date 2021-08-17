package com.chinthaka.chinthaka_beta.data.entities

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Category(
    val categoryId: String = "",
    val categoryName: String = "",
    val categoryImageUrl: String = "",
    @get:Exclude var isSelected: Boolean = true,
    val isActive: Boolean = true
)