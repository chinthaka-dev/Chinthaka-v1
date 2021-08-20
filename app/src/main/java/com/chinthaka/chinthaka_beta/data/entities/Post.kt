package com.chinthaka.chinthaka_beta.data.entities

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Post(
    val id: String = "",
    val authorUId: String = "",
    @get:Exclude var authorUserName: String = "",
    @get:Exclude var authorProfilePictureUrl: String = "",
    val text: String = "",
    val answer: Map<String, String> = mapOf(),
    val imageUrl: String = "",
    val date: Long = 0L,
    val category: String = "",
    @get:Exclude var isLiked: Boolean = false, // if current user has already liked the current post
    @get:Exclude var isLiking: Boolean = false, // during Network request, once Like button is pressed
    var likedBy: List<String> = listOf(), // Firebase Charges on Read Operations. So, this list can be huge. Please NOTE this!!!
    @get:Exclude var isAnswered: Boolean = false, // if current user has already answered the current post
    @get:Exclude var isAnswering: Boolean = false, // during Network request, once Done button of Submit Answer fragment is pressed
    var answeredBy: List<String> = listOf(),
    @get:Exclude var isBookmarked: Boolean = false,
    @get:Exclude var isBookmarking: Boolean = false
)