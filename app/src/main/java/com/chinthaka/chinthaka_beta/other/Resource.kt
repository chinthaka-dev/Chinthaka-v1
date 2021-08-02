package com.chinthaka.chinthaka_beta.other

sealed class Resource<T>(val data : T? = null, val message: String? = null) {

    /** 1. This class is for checking a specific network call (or Resource) resulted in Error/ Success / Loading to show the Progress Bar
     *  2. Useful for MVVM Architecture
     * */

    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)

}