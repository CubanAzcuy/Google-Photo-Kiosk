package gives.robert.kiosk.gphotos.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class UserPreferencesRecord(
    val authToken: String? = null,
    val selectedAlbumIds: Set<String> = emptySet()
)

class UserPreferences(context: Context) {

    private val dataStore = getDataStore(context)

    var userPreferencesRecord = UserPreferencesRecord()

    val preferencesFlow = dataStore.data.map {
        UserPreferencesRecord(
            it.toPreferences()[authStringKey],
            it.toPreferences()[savedAlbumsKeys] ?: emptySet()
        )
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            preferencesFlow.collect {
                userPreferencesRecord = it
            }
        }
    }

    suspend fun saveAuthToken(authToken: String) {
        dataStore.edit {
            it[authStringKey] = authToken
        }
    }

    suspend fun clearAuthToken() {
        dataStore.edit {
            it.remove(authStringKey)
        }
    }

    suspend fun setSelectedAlbums(albumIds: Set<String>) {
        dataStore.edit {
            it[savedAlbumsKeys] = albumIds
        }
    }

    companion object {
        private val authStringKey = stringPreferencesKey("auth_string_key")
        private val savedAlbumsKeys = stringSetPreferencesKey("saved_albums_key")

        fun getDataStore(context: Context): DataStore<Preferences> = getDataStore(
            producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
        )
    }
}
