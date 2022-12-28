package gives.robert.kiosk.gphotos.utils.providers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import gives.robert.kiosk.gphotos.utils.dataStoreFileName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class UserPreferencesRecord(
    val authToken: String? = null,
    val selectedAlbumIds: Set<String> = emptySet(),
    val shouldFetch: Boolean = false,
)

class UserPreferences(context: Context) {

    private val dataStore = getDataStore(context)

    val userPreferencesRecord
        get() = preferencesFlow.value

    val preferencesFlow = MutableStateFlow(UserPreferencesRecord())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.data.map {
                UserPreferencesRecord(
                    it.toPreferences()[authStringKey],
                    it.toPreferences()[savedAlbumsKeys] ?: emptySet(),
                    it.toPreferences()[shouldFetchKey] ?: false
                )
            }.collect{
                preferencesFlow.emit(it)
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

    suspend fun setShouldFetch(shouldFetch: Boolean) {
        dataStore.edit {
            it[shouldFetchKey] = shouldFetch
        }
    }

    companion object {
        private val authStringKey = stringPreferencesKey("auth_string_key")
        private val savedAlbumsKeys = stringSetPreferencesKey("saved_albums_key")
        private val shouldFetchKey = booleanPreferencesKey("should_fetch_key")

        private var instance: UserPreferences? = null

        fun getDataStore(context: Context): DataStore<Preferences> =
            gives.robert.kiosk.gphotos.utils.getDataStore(
                producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
            )

        fun getInstance(context: Context): UserPreferences {
            if (instance == null) {
                init(context)
            }
            return instance!!
        }

        fun init(context: Context) {
            instance = UserPreferences(context)
        }
    }
}
