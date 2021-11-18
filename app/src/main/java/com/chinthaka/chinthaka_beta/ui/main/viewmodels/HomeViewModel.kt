package com.chinthaka.chinthaka_beta.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.chinthaka.chinthaka_beta.data.entities.Category
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.data.pagingsource.FeedPostsPagingSource
import com.chinthaka.chinthaka_beta.other.Constants.PAGE_SIZE
import com.chinthaka.chinthaka_beta.other.Event
import com.chinthaka.chinthaka_beta.other.Resource
import com.chinthaka.chinthaka_beta.repositories.MainRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : BasePostViewModel(repository, dispatcher) {

    private val _getUserStatus = MutableLiveData<Event<Resource<User>>>()
    val getUserStatus: LiveData<Event<Resource<User>>> = _getUserStatus

    val pagingFlow = Pager(PagingConfig(PAGE_SIZE)){
        FeedPostsPagingSource(FirebaseFirestore.getInstance())
    }.flow.cachedIn(viewModelScope)

    private val _posts = MutableLiveData<Event<Resource<List<Post>>>>()
    val posts: LiveData<Event<Resource<List<Post>>>>
        get() = _posts

    init {
        loadPosts()
    }

    fun loadPosts(){
        _posts.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getRecentPosts()
            _posts.postValue(Event(result))
        }
    }

    fun getUser(userId: String){
        _getUserStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getUser(userId)
            _getUserStatus.postValue(Event(result))
        }
    }

}