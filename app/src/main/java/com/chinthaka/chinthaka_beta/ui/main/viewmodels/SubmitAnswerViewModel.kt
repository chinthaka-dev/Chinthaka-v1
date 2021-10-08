package com.chinthaka.chinthaka_beta.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chinthaka.chinthaka_beta.other.Event
import com.chinthaka.chinthaka_beta.other.Resource
import com.chinthaka.chinthaka_beta.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmitAnswerViewModel @Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _answerPostStatus = MutableLiveData<Event<Resource<Boolean>>>()
    val answerPostStatus: LiveData<Event<Resource<Boolean>>> = _answerPostStatus

    fun submitAnswerForPost(postId: String) {
        _answerPostStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.submitAnswerForPost(postId)
            _answerPostStatus.postValue(Event(result))
        }
    }

    fun updateAnswerViewedByForPostId(postId: String) {
        _answerPostStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.updateAnswerViewedByForPostId(postId)
            _answerPostStatus.postValue(Event(result))
        }
    }
}