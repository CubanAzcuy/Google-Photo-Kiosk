package gives.robert.kiosk.gphotos.utils.providers

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import gives.robert.kiosk.gphotos.Database
import gives.robert.kiosk.gphotos.SeenPhotosQueries

class DatabaseQueryProvider(context: Context) {
    private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "test.db")
    val database = Database(driver)

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