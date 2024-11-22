package com.gity.feliyaattendance.admin.ui.main.detail.announcement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gity.feliyaattendance.admin.data.model.Announcement
import com.gity.feliyaattendance.repository.Repository
import kotlinx.coroutines.launch

class AdminAnnouncementDetailViewModel(private val repository: Repository) : ViewModel() {

    private val _announcementDetail = MutableLiveData<Result<Announcement>>()
    val announcementDetail: LiveData<Result<Announcement>> get() = _announcementDetail

    private val _deleteAnnouncement = MutableLiveData<Result<Unit>>()
    val deleteAnnouncement: LiveData<Result<Unit>> get() = _deleteAnnouncement

    fun getAnnouncementDetail(announcementId: String) {
        viewModelScope.launch {
            _announcementDetail.postValue(repository.getDetailAnnouncement(announcementId))
        }
    }

    fun deleteAnnouncement(announcementId: String) {
        viewModelScope.launch {
            _deleteAnnouncement.postValue(repository.deleteAnnouncement(announcementId))
        }
    }

}