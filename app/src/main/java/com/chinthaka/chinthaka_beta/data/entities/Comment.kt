package com.chinthaka.chinthaka_beta.data.entities

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Comment(
    val commentId: String,
    val postId: String,
    val userId: String,
    @get:Exclude var userName: String,
    @get:Exclude var profilePictureUrl: String,
    val comment: String,
    val date: Long = System.currentTimeMillis()
)