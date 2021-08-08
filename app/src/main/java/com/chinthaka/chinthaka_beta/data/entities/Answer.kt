package com.chinthaka.chinthaka_beta.data.entities

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Answer(
    val postId: String,
    val answerText: String,
    @get:Exclude var answerExplanation: String,
    @get:Exclude var answerPictureUrl: String,
)