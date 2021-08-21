package com.chinthaka.chinthaka_beta.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.other.Event
import com.chinthaka.chinthaka_beta.other.Resource
import com.chinthaka.chinthaka_beta.repositories.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BasePostViewModel(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _likePostStatus = MutableLiveData<Event<Resource<Boolean>>>()
    val likePostStatus: LiveData<Event<Resource<Boolean>>> = _likePostStatus

    private val _deletePostStatus = MutableLiveData<Event<Resource<Post>>>()
    val deletePostStatus: LiveData<Event<Resource<Post>>> = _deletePostStatus

    private val _likedByUsers = MutableLiveData<Event<Resource<List<User>>>>()
    val likedByUsers: LiveData<Event<Resource<List<User>>>> = _likedByUsers

    private val _answeredByUsers = MutableLiveData<Event<Resource<List<User>>>>()
    val answeredByUsers: LiveData<Event<Resource<List<User>>>> = _answeredByUsers

    private val _bookmarkPostStatus = MutableLiveData<Event<Resource<Boolean>>>()
    val bookmarkPostStatus: LiveData<Event<Resource<Boolean>>> = _bookmarkPostStatus

    private val _answerViewedByUpdateStatus = MutableLiveData<Event<Resource<Boolean>>>()
    val answerViewedByUpdateStatus: LiveData<Event<Resource<Boolean>>> = _answerViewedByUpdateStatus

    private val _attemptedByUpdateStatus = MutableLiveData<Event<Resource<Boolean>>>()
    val attemptedByUpdateStatus: LiveData<Event<Resource<Boolean>>> = _attemptedByUpdateStatus

    fun getUsers(userIds: List<String>){
        if(userIds.isEmpty()) return
        _likedByUsers.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher){
            val result = repository.getUsers(userIds)
            _likedByUsers.postValue(Event(result))
        }
    }

    fun toggleLikeForPost(post: Post){
        _likePostStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher){
            val result = repository.toggleLikeForPost(post)
            _likePostStatus.postValue(Event(result))
        }
    }

    fun deletePost(post: Post){
        _deletePostStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher){
            val result = repository.deletePost(post)
            _deletePostStatus.postValue(Event(result))
        }
    }

    fun toggleBookmarkForPost(post: Post){
        _bookmarkPostStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher){
            val result = repository.toggleBookmarkForPost(post.id)
            _bookmarkPostStatus.postValue(Event(result))
        }
    }

    fun updateAnswerViewedByForPost(post: Post){
        _answerViewedByUpdateStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.updateAnswerViewedByForPost(post)
            _answerViewedByUpdateStatus.postValue(Event(result))
        }
    }

    fun updateAttemptedByForPost(post: Post){
        _attemptedByUpdateStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.updateAttemptedByForPost(post)
            _attemptedByUpdateStatus.postValue(Event(result))
        }
    }
}