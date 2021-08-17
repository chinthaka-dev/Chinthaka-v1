package com.chinthaka.chinthaka_beta.data.entities


data class CategoryUpdate(
    val uidToUpdate: String = "",
    val updatedInterests: List<String> = listOf()
)