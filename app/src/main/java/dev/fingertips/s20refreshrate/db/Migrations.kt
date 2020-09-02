package dev.fingertips.s20refreshrate.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Remove 96Hz option, change any app that is currently set for 96Hz to 120Hz
 */
val MIGRATION_1_2 = object: Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("UPDATE apps SET mode = 2 WHERE mode = 3")
    }
}