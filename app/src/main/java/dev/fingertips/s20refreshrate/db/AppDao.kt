package dev.fingertips.s20refreshrate.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(vararg apps: App)

    @Query("SELECT * FROM apps")
    suspend fun getAllApps(): List<App>

    @Query("SELECT * FROM apps")
    fun getAllAppsAsLiveData(): LiveData<List<App>>

    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    suspend fun getApp(packageName: String): App?

    @Update
    suspend fun updateApp(app: App)

    @Delete
    suspend fun deleteApp(app: App)
}