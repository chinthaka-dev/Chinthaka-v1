package com.chinthaka.chinthaka_beta.other

import androidx.lifecycle.Observer

class Event<out T>(private val content: T) {
    /***
     * With this class, we prevent that the Snakebars don't appear twice when we rotate the device, after an Error has occured.
     */

    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {

        return if(!hasBeenHandled) {
            hasBeenHandled = true
            content
        } else null
    }

    fun peekContent() = content
}

class EventObserver<T>(
    private inline val onError: ((String) -> Unit)? = null,
    private inline val onLoading: (() -> Unit)? = null,
    private inline val onSuccess: (T) -> Unit
) : Observer<Event<Resource<T>>> {

    override fun onChanged(t: Event<Resource<T>>?) {
        when(val content = t?.peekContent()){
            is Resource.Success -> {
                content.data?.let(onSuccess)
            }

            is Resource.Error -> {
                t.getContentIfNotHandled()?.let {
                    onError?.let {
                            error -> error(it.message!!)
                    }
                }
            }

            is Resource.Loading -> {
                onLoading?.let { loading ->
                    loading()
                }
            }
        }
    }
}