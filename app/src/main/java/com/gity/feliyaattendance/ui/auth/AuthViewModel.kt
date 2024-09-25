package com.gity.feliyaattendance.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: Repository) : ViewModel() {

    //    Using MutableLiveData to observer login results
    private val _loginResult = MutableLiveData<Result<String>>()
    val loginResult: LiveData<Result<String>> = _loginResult

    private val _registrationResult = MutableLiveData<Result<Unit>>()
    val registrationResult: LiveData<Result<Unit>> = _registrationResult

    //    Login Function
    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            _loginResult.postValue(result)
        }
    }

    // Register function
    fun register(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.registerUser(email, password)
            _registrationResult.postValue(result)
        }
    }
}