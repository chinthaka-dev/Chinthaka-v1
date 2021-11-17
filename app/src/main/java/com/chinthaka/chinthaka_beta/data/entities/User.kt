package com.chinthaka.chinthaka_beta.data.entities

import com.chinthaka.chinthaka_beta.other.Constants.DEFAULT_PROFILE_PICTURE_URL
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val userId : String = "",
    val userName : String = "",
    val profilePictureUrl: String = DEFAULT_PROFILE_PICTURE_URL,
    val description: String = "",
    var follows: List<String> = listOf(),
    val selectedInterests: List<String> = listOf(),
    var bookmarks: List<String> = listOf(),
    var postsAnswered: List<String> = listOf(),
    var postsAttempted: MutableList<String> = mutableListOf(),
    var creator: Boolean = false,
    @get:Exclude var isFollowing: Boolean = false,
)