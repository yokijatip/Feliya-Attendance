package com.gity.feliyaattendance.admin.data.model

import com.google.firebase.Timestamp

data class Worker(
    var id: String? = "",
    var created: Timestamp? = null,
    var email: String? = "",
    var name: String? = "",
    var profileImageUrl: String? = "",
    var role: String? = "",
    var status: String? = "",
    var workerId: String? = ""
)
