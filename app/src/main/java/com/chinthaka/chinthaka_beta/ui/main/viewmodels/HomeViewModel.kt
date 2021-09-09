package com.chinthaka.chinthaka_beta.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
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

    private val _getUser = MutableLiveData<Event<Resource<User>>>()
    val getUser: LiveData<Event<Resource<User>>> = _getUser

    val pagingFlow = Pager(PagingConfig(PAGE_SIZE)){
        FeedPostsPagingSource(FirebaseFirestore.getInstance())
    }.flow.cachedIn(viewModelScope)

    fun getUser(userId: String){
        _getUser.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getUser(userId)
            _getUser.postValue(Event(result))
        }
    }

}