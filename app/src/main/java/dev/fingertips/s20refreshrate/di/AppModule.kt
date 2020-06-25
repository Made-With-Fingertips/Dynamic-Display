package dev.fingertips.s20refreshrate.di

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import dev.fingertips.s20refreshrate.RefreshApplication
import javax.inject.Singleton

@Module
class AppModule(private val refreshApplication: RefreshApplication) {

    @Provides
    @Singleton
    fun provideApplication(): RefreshApplication = refreshApplication

    @Provides
    @Singleton
    fun provideContext(): Context = refreshApplication

    @Provides
    @Singleton
    fun provideContentResolver(): ContentResolver = refreshApplication.contentResolver

    @Provides
    @Singleton
    fun providePackageManager(): PackageManager = refreshApplication.packageManager
}