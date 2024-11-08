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

    private val _roles = MutableLiveData<Result<List<String>>>()
    val roles: LiveData<Result<List<String>>> = _roles

    //    Loading State
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    //    Login Function
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.loginUser(email, password)
            _isLoading.value = false
            _loginResult.postValue(result)
        }
    }

    // Register function
    fun register(email: String, password: String, name: String, role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.registerUser(email, password, name, role)
            _isLoading.value = false
            _registrationResult.postValue(result)
        }
    }

    //  Get Roles
    fun getRoles() {
        viewModelScope.launch {
            _isLoading.value = true
            _roles.value = repository.fetchRoles()
            _isLoading.value = false
        }
    }
}