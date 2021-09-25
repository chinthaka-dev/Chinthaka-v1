package com.chinthaka.chinthaka_beta.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chinthaka.chinthaka_beta.data.entities.Category
import com.chinthaka.chinthaka_beta.data.entities.CategoryUpdate
import com.chinthaka.chinthaka_beta.other.Event
import com.chinthaka.chinthaka_beta.other.Resource
import com.chinthaka.chinthaka_beta.repositories.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterestsViewModel @Inject constructor(
    private val repository: CategoryRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _interests = MutableLiveData<Event<Resource<List<Category>>>>()
    val interests: LiveData<Event<Resource<List<Category>>>>
        get() = _interests

    private val _submitInterestsStatus = MutableLiveData<Event<Resource<Any>>>()
    val submitInterestsStatus: LiveData<Event<Resource<Any>>> = _submitInterestsStatus

    init {
        loadInterests()
    }

    fun loadInterests(){
        _interests.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getCategories()
            _interests.postValue(Event(result))
        }
    }

    fun updateInterests(categoryUpdate: CategoryUpdate) {
        viewModelScope.launch(dispatcher) {
            val result = repository.updateInterestsForAUser(categoryUpdate)
            _submitInterestsStatus.postValue(Event(result))
        }
    }
}