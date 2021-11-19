package com.chinthaka.chinthaka_beta.other

import androidx.paging.PagingSource
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.google.firebase.firestore.QuerySnapshot
import java.lang.Exception

inline fun safeCallFeed(action: () -> PagingSource.LoadResult.Page<QuerySnapshot, Post>): PagingSource.LoadResult<QuerySnapshot, Post> {
    return try {
        action()
    } catch (e: Exception){
        PagingSource.LoadResult.Page(
            emptyList(),
            null,
            null
        )
    }
}

inline fun<T> safeCall(action: () -> Resource<T>): Resource<T> {
    return try {
        action()
    } catch (e: Exception){
        Resource.Error(e.localizedMessage ?: "An unknown error occurred")
    }
}