package gives.robert.kiosk.gphotos.utils.providers

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import gives.robert.kiosk.gphotos.Database
import gives.robert.kiosk.gphotos.SeenAlbums
import gives.robert.kiosk.gphotos.SeenPhotos
import gives.robert.kiosk.gphotos.SeenPhotosQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DatabaseQueryProvider(context: Context) {
    private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "test.db")
    val database = Database(driver)

    fun photoList(): Flow<List<SeenPhotos>> {
        return database.seenPhotosQueries.selectAll().asFlow().mapToList()
    }

    fun albumList(): Flow<List<SeenAlbums>> {
        return database.seenAlbumsQueries.selectAll().asFlow().mapToList()
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