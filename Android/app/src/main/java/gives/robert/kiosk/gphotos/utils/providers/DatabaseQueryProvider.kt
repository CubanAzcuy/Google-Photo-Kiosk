package gives.robert.kiosk.gphotos.utils.providers

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import gives.robert.kiosk.gphotos.Albums
import gives.robert.kiosk.gphotos.Database
import gives.robert.kiosk.gphotos.Photos
import kotlinx.coroutines.flow.Flow

class DatabaseQueryProvider(context: Context) {
    private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "photo_db.db")
    val database = Database(driver)

    fun photoList(): Flow<List<Photos>> {
        return database.photosQueries.selectAll().asFlow().mapToList()
    }

    fun albumList(): Flow<List<Albums>> {
        return database.albumsQueries.selectAll().asFlow().mapToList()
    }

    companion object {
        @Volatile
        private lateinit var instance: DatabaseQueryProvider

        fun getInstance(context: Context): DatabaseQueryProvider {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = DatabaseQueryProvider(context)
                }
                return instance
            }
        }
    }
}