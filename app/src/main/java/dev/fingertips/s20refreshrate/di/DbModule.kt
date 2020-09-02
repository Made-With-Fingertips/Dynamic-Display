package dev.fingertips.s20refreshrate.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dev.fingertips.s20refreshrate.db.AppDao
import dev.fingertips.s20refreshrate.db.AppDatabase
import dev.fingertips.s20refreshrate.db.MIGRATION_1_2
import javax.inject.Singleton

@Module
class DbModule {

    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "apps.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideAppDao(appDatabase: AppDatabase): AppDao = appDatabase.appDao()
}