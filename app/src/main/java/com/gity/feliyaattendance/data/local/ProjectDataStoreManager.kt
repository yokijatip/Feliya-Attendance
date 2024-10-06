package com.gity.feliyaattendance.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectDataStoreManager(private val context: Context) {
    companion object {
        private val Context.dataStore by preferencesDataStore("project_data")
        private val PROJECT_ID = stringPreferencesKey("project_id")
        private val PROJECT_NAME = stringPreferencesKey("project_name")
        private val PROJECT_LOCATION = stringPreferencesKey("project_location")
    }

    suspend fun saveProjectData(id: String, name: String, location: String) {
        context.dataStore.edit { preferences ->
            preferences[PROJECT_ID] = id
            preferences[PROJECT_NAME] = name
            preferences[PROJECT_LOCATION] = location
        }
    }

    val projectId: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PROJECT_ID] }

    val projectName: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PROJECT_NAME] }

    val projectLocation: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PROJECT_LOCATION] }

    suspend fun clearProjectData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}