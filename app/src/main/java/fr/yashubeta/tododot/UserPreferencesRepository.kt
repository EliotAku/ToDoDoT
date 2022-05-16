package fr.yashubeta.tododot

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreferences(val showSubTasks: Boolean)

/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val SHOW_SUB_TASKS = booleanPreferencesKey("show_sub_tasks")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        val showSubTasks = preferences[PreferencesKeys.SHOW_SUB_TASKS] ?: false

        UserPreferences(showSubTasks)
    }

    suspend fun updateShowSubTasks(showSubTasks: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_SUB_TASKS] = showSubTasks
        }
    }

}