package com.gity.feliyaattendance.ui.main.settings.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.admin.data.model.Worker
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AccountViewModel(private val repository: Repository) : ViewModel() {
    private val _userDetails = MutableLiveData<Result<Worker>>()
    val userDetails: LiveData<Result<Worker>> get() = _userDetails

    private val _profileImageUploadResult = MutableLiveData<Result<String>>()
    val profileImageUploadResult: LiveData<Result<String>> = _profileImageUploadResult

    fun loadUserDetails(userId: String) {
        viewModelScope.launch {
            _userDetails.postValue(repository.getDetailAccount(userId))
        }
    }

    fun uploadProfileImage(imageUrl: String) {
        viewModelScope.launch {
            _profileImageUploadResult.value = repository.uploadUserProfileImage(imageUrl)
        }
    }
}