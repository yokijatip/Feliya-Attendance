package com.gity.feliyaattendance.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Setting (
    val tvSetting: String,
    val ivSetting: Int
): Parcelable