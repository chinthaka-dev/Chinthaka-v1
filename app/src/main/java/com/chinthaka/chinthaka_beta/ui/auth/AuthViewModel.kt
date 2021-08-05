package com.chinthaka.chinthaka_beta.ui.auth

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.other.Constants.MAX_USERNAME_LENGTH
import com.chinthaka.chinthaka_beta.other.Constants.MIN_PASSWORD_LENGTH
import com.chinthaka.chinthaka_beta.other.Constants.MIN_USERNAME_LENGTH
import com.chinthaka.chinthaka_beta.other.Event
import com.chinthaka.chinthaka_beta.other.Resource
import com.chinthaka.chinthaka_beta.repositories.AuthRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val applicationContext: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    /**
     *  NOTE : A ViewModel should be independent of Activity Context
     * */

    // Mutable Value
    private val _registerStatus = MutableLiveData<Event<Resource<AuthResult>>>()

    // Immutable Value
    val registerStatus: LiveData<Event<Resource<AuthResult>>> = _registerStatus

    // Mutable Value
    private val _loginStatus = MutableLiveData<Event<Resource<AuthResult>>>()

    // Immutable Value
    val loginStatus: LiveData<Event<Resource<AuthResult>>> = _loginStatus

    // Mutable Value
    private val _addNewUserStatus = MutableLiveData<Event<Resource<FirebaseUser>>>()

    // Immutable Value
    val addNewUserStatus: LiveData<Event<Resource<FirebaseUser>>> = _addNewUserStatus

    fun register(email: String, userName: String, password: String, repeatedPassword: String){
        // Validate User Inputs
        val error = if(email.isEmpty() || userName.isEmpty() || password.isEmpty() || repeatedPassword.isEmpty()){
            applicationContext.getString(R.string.error_input_empty)
        } else if(password != repeatedPassword){
            applicationContext.getString(R.string.error_incorrectly_repeated_password)
        } else if(userName.length < MIN_USERNAME_LENGTH){
            applicationContext.getString(R.string.error_username_too_short, MIN_USERNAME_LENGTH)
        } else if(userName.length > MAX_USERNAME_LENGTH){
            applicationContext.getString(R.string.error_username_too_long, MAX_USERNAME_LENGTH)
        } else if(password.length < MIN_PASSWORD_LENGTH){
            applicationContext.getString(R.string.error_password_too_short)
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            applicationContext.getString(R.string.error_not_a_valid_email)
        } else null

        error?.let {
            _registerStatus.postValue(Event(Resource.Error(it)))
            return
        }

        _registerStatus.postValue(Event(Resource.Loading()))

        viewModelScope.launch(dispatcher) {
            val result = repository.register(email, userName, password)
            _registerStatus.postValue(Event(result))
        }
    }

    fun login(email: String, password: String){

        if(email.isEmpty() || password.isEmpty()){
            val error = applicationContext.getString(R.string.error_input_empty)
            _loginStatus.postValue(Event(Resource.Error(error)))
        } else{
            _loginStatus.postValue(Event(Resource.Loading()))
            viewModelScope.launch(dispatcher){
                val result = repository.login(email, password)
                _loginStatus.postValue(Event(result))
            }
        }
    }

    fun addNewUserViaSocialLogin(user: FirebaseUser?) {
        if(user == null){
            val error = "User is NULL";
            _addNewUserStatus.postValue(Event(Resource.Error(error)))
        }
        else{
            _addNewUserStatus.postValue(Event(Resource.Loading()))
            viewModelScope.launch(dispatcher) {
                val result = repository.addNewUserViaSocialLogin(user)
                _addNewUserStatus.postValue(Event(result))
            }
        }
    }
}