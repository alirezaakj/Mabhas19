package ir.mabhas19.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProjectEntity::class, ContextEntity::class, DraftEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MabhasDatabase : RoomDatabase() {
    abstract fun projects(): ProjectDao
}
