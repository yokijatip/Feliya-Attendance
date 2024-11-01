package com.gity.feliyaattendance.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailWorkerMenu(
    val tvDetailWorkerMenu: String,
    val ivDetailWorkerMenu: Int
): Parcelable
