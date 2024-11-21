package com.gity.feliyaattendance.admin.data.model

import com.google.firebase.Timestamp

data class Announcement(
    var announcement: String? = "",
    var createdAt: Timestamp? = null,
    var createdBy: String? = "",
    var createdByName: String? = "",
    var createdByEmail: String? = "",
    var imageAnnouncement: String? = "",
)