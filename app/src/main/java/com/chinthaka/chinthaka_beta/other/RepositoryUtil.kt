package com.chinthaka.chinthaka_beta.other

import java.lang.Exception

inline fun<T> safeCall(action: () -> Resource<T>): Resource<T> {
    return try {
        action()
    } catch (e: Exception){
        Resource.Error(e.localizedMessage ?: "An unknown error occurred")
    }
}