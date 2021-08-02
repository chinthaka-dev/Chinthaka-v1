package com.chinthaka.chinthaka_beta.data.entities

import android.net.Uri

data class ProfileUpdate(
    val uidToUpdate: String = "",
    val userName: String = "",
    val description: String = "",
    val profilePictureUri: Uri? = null
)