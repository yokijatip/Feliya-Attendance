package com.gity.feliyaattendance.admin.ui.main.announcement

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AdminAnnouncementViewModel(private val repository: Repository) : ViewModel() {

    private val _createAnnouncement = MutableLiveData<Result<Unit>>()
    val createAnnouncement: MutableLiveData<Result<Unit>> = _createAnnouncement

    fun addPost(
        announcement: String,
        createdBy: String,
        createdByName: String,
        createdByEmail: String,
        imageAnnouncement: String
    ) {
        viewModelScope.launch {
            val result = repository.createAnnouncement(
                announcement,
                createdBy,
                createdByName,
                createdByEmail,
                imageAnnouncement
            )
            _createAnnouncement.postValue(result)
        }
    }

}