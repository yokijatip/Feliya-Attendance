package com.gity.feliyaattendance.admin.ui.main.announcement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.admin.data.model.Announcement
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch
import java.util.Date

class AdminAnnouncementViewModel(private val repository: Repository) : ViewModel() {

    private val _createAnnouncement = MutableLiveData<Result<Unit>>()
    val createAnnouncement: MutableLiveData<Result<Unit>> = _createAnnouncement

    private val _announcementList = MutableLiveData<Result<List<Announcement>>>()
    val announcementList: LiveData<Result<List<Announcement>>> = _announcementList

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

    fun fetchAnnouncements(selectedDate: Date? = null) {
        viewModelScope.launch {
            val result = repository.fetchAnnouncementList(selectedDate)
            _announcementList.postValue(result)
        }
    }

}