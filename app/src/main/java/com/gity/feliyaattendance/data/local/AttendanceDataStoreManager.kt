package com.gity.feliyaattendance.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
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
        private val DATE = stringPreferencesKey("date")
        private val CLOCK_IN = stringPreferencesKey("clock_in")
        private val CLOCK_OUT = stringPreferencesKey("clock_out")
        private val IMAGE_URL_IN = stringPreferencesKey("image_url_in")
        private val IMAGE_URL_OUT = stringPreferencesKey("image_url_out")
        private val DESCRIPTION = stringPreferencesKey("description")
        private val STATUS = stringPreferencesKey("status")
        private val WORK_HOURS = stringPreferencesKey("work_hours")
        private val WORK_HOURS_OVERTIME = stringPreferencesKey("work_hours_overtime")
    }

    suspend fun saveClockInData(
        userId: String,
        projectId: String,
        date: Date,
        clockIn: Timestamp,
        imageUrlIn: String,
        ) {
        context.dataStore.edit { pref ->
            pref[USER_ID] = userId
            pref[PROJECT_ID] = projectId
            pref[DATE] = date.toString()
            pref[CLOCK_IN] = clockIn.seconds.toString()
            pref[IMAGE_URL_IN] = imageUrlIn

        }
    }

    suspend fun saveClockOutData(
        clockOut: Timestamp,
        imageUrlOut: String,
        status: String,
        workHours: Int,
        workHoursOvertime: Int,
        description: String
    ) {
        context.dataStore.edit { pref ->
            pref[CLOCK_OUT] = clockOut.toString()
            pref[IMAGE_URL_OUT] = imageUrlOut
            pref[STATUS] = status
            pref[WORK_HOURS] = workHours.toString()
            pref[WORK_HOURS_OVERTIME] = workHoursOvertime.toString()
            pref[DESCRIPTION] = description
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

    val date: Flow<String?> = context.dataStore.data
        .map { pref -> pref[DATE] }

    val clockIn: Flow<String?> = context.dataStore.data
        .map { pref -> pref[CLOCK_IN] }

    val clockOut: Flow<String?> = context.dataStore.data
        .map { pref -> pref[CLOCK_OUT] }

    val imageUrlIn: Flow<String?> = context.dataStore.data
        .map { pref -> pref[IMAGE_URL_IN] }

    val imageUrlOut: Flow<String?> = context.dataStore.data
        .map { pref -> pref[IMAGE_URL_OUT] }


    val description: Flow<String?> = context.dataStore.data
        .map { pref -> pref[DESCRIPTION] }


    val status: Flow<String?> = context.dataStore.data
        .map { pref -> pref[STATUS] }

    val workHours: Flow<String?> = context.dataStore.data
        .map { pref -> pref[WORK_HOURS] }

    val workHoursOvertime: Flow<String?> = context.dataStore.data
        .map { pref -> pref[WORK_HOURS_OVERTIME] }
}