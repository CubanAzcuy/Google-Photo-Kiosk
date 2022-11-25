package gives.robert.kiosk.gphotos.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UserPreferences(context: Context) {

    private val dataStore = getDataStore(context)

    var authToken: String? = null
    val authTokenFlow: Flow<String?> = dataStore.data.map {
        it[authStringKey]
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            authTokenFlow.collect {
                authToken = it
            }
        }
    }

    suspend fun saveAuthToken(authToken: String) {
        dataStore.edit {
            it[authStringKey] = authToken
        }
    }

    companion object {
        private val authStringKey = stringPreferencesKey("auth_string_key")

        fun getDataStore(context: Context): DataStore<Preferences> = getDataStore(
            producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
        )
    }
}
