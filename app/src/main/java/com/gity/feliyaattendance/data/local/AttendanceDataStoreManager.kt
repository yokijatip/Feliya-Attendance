package com.gity.feliyaattendance.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class AttendanceDataStoreManager(private val context: Context) {
    companion object {
        private val Context.dataStore by preferencesDataStore("attendance_data")
        private val USER_ID = stringPreferencesKey("user_id")
        private val PROJECT_ID = stringPreferencesKey("project_id")
        private val DATE = longPreferencesKey("date")
        private val CLOCK_IN = longPreferencesKey("clock_in")
        private val CLOCK_OUT = longPreferencesKey("clock_out")
        private val IMAGE_URL_IN = stringPreferencesKey("image_url_in")
        private val IMAGE_URL_OUT = stringPreferencesKey("image_url_out")
        private val DESCRIPTION = stringPreferencesKey("description")
        private val STATUS = stringPreferencesKey("status")
        private val WORK_HOURS = intPreferencesKey("work_hours")
        private val WORK_HOURS_OVERTIME = intPreferencesKey(
            "work_hours_overtime"
        )
        private val TOTAL_WORK_HOURS = intPreferencesKey("total_work_hours")
    }

    suspend fun saveClockInData(
        userId: String,
        projectId: String,
        date: Timestamp,
        clockIn: Timestamp,
        imageUrlIn: String,
    ) {
        context.dataStore.edit { pref ->
            pref[USER_ID] = userId
            pref[PROJECT_ID] = projectId
            pref[DATE] = date.seconds * 1000 + date.nanoseconds / 1000000
            pref[CLOCK_IN] = clockIn.seconds * 1000 + clockIn.nanoseconds / 1000000
            pref[IMAGE_URL_IN] = imageUrlIn

        }
    }

    suspend fun saveClockOutData(
        clockOut: Timestamp,
        imageUrlOut: String,
        status: String,
        workHours: Int,
        workHoursOvertime: Int,
        description: String,
        totalWorkHours: Int
    ) {
        context.dataStore.edit { pref ->
            pref[CLOCK_OUT] = clockOut.seconds * 1000 + clockOut.nanoseconds / 1000000
            pref[IMAGE_URL_OUT] = imageUrlOut
            pref[STATUS] = status
            pref[WORK_HOURS] = workHours
            pref[WORK_HOURS_OVERTIME] = workHoursOvertime
            pref[DESCRIPTION] = description
            pref[TOTAL_WORK_HOURS] = totalWorkHours
        }
    }

    suspend fun clearClockInOutData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }


    val userId: Flow<String?> = context.dataStore.data
        .map { pref -> pref[USER_ID] }

    val projectId: Flow<String?> = context.dataStore.data
        .map { pref -> pref[PROJECT_ID] }

    val date: Flow<Timestamp?> = context.dataStore.data
        .map { pref ->
            pref[CLOCK_IN]?.let { millis ->
                Timestamp(Date(millis))
            }
        }


    // Fungsi untuk mengambil clock in sebagai Timestamp
    val clockIn: Flow<Timestamp?> = context.dataStore.data
        .map { pref ->
            pref[CLOCK_IN]?.let { millis ->
                Timestamp(Date(millis))
            }
        }

    // Fungsi untuk mengambil clock out sebagai Timestamp
    val clockOut: Flow<Timestamp?> = context.dataStore.data
        .map { pref ->
            pref[CLOCK_OUT]?.let { millis ->
                Timestamp(Date(millis))
            }
        }

    val imageUrlIn: Flow<String?> = context.dataStore.data
        .map { pref -> pref[IMAGE_URL_IN] }

    val imageUrlOut: Flow<String?> = context.dataStore.data
        .map { pref -> pref[IMAGE_URL_OUT] }

    val description: Flow<String?> = context.dataStore.data
        .map { pref -> pref[DESCRIPTION] }

    val status: Flow<String?> = context.dataStore.data
        .map { pref -> pref[STATUS] }

    val workHours: Flow<Int?> = context.dataStore.data
        .map { pref -> pref[WORK_HOURS] }

    val workHoursOvertime: Flow<Int?> = context.dataStore.data
        .map { pref -> pref[WORK_HOURS_OVERTIME] }

    val totalWorkHours: Flow<Int?> = context.dataStore.data
        .map { pref -> pref[TOTAL_WORK_HOURS] }
}