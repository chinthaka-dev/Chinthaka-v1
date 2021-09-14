package com.chinthaka.chinthaka_beta.ui.main.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.data.entities.Category
import com.chinthaka.chinthaka_beta.other.Event
import com.chinthaka.chinthaka_beta.other.Resource
import com.chinthaka.chinthaka_beta.repositories.CategoryRepository
import com.chinthaka.chinthaka_beta.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val repository: MainRepository,
    private val categoryRepository: CategoryRepository,
    private val applicationContext: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _createPostStatus = MutableLiveData<Event<Resource<Any>>>()
    val createPostStatus : LiveData<Event<Resource<Any>>> = _createPostStatus

    private val _curImageUri = MutableLiveData<Uri>()
    val curImageUri: LiveData<Uri> = _curImageUri

    private val _getCategoriesStatus = MutableLiveData<Event<Resource<List<Category>>>>()
    val getCategoriesStatus: LiveData<Event<Resource<List<Category>>>> = _getCategoriesStatus

    fun createPost(imageUri : Uri, text : String, category: String, answerText: String, answerDescription: String){
        if(text.isEmpty() || category.isEmpty() || answerText.isEmpty()){
            val error = applicationContext.getString(R.string.error_input_empty)
            _createPostStatus.postValue(Event(Resource.Error(error)))
        } else{
            _createPostStatus.postValue(Event(Resource.Loading()))
            viewModelScope.launch(dispatcher){
                val result = repository.createPost(imageUri, text, category, answerText, answerDescription)
                _createPostStatus.postValue(Event(result))
            }
        }
    }

    fun setCurImageUri(uri : Uri){
        _curImageUri.postValue(uri)
    }

    fun getAllCategories(){
        _getCategoriesStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher){
            val result = categoryRepository.getCategories()
            _getCategoriesStatus.postValue(Event(result))
        }
    }
}