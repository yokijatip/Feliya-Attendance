package com.gity.feliyaattendance.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Project(
    var projectId: String = "",
    var projectName: String = "",
    var location: String = "",
    var startDate: Timestamp? = null, // Menggunakan Timestamp
    var endDate: Timestamp? = null,
    val status: String = "",
    var description: String = ""
) : Parcelable

