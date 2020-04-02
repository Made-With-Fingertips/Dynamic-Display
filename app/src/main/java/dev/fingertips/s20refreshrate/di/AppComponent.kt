package dev.fingertips.s20refreshrate.di

import dagger.Component
import dev.fingertips.s20refreshrate.RefreshApplication
import dev.fingertips.s20refreshrate.RefreshService
import dev.fingertips.s20refreshrate.ui.apps.AppsFragment
import dev.fingertips.s20refreshrate.ui.main.MainActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DbModule::class])
interface AppComponent {
    fun inject(refreshApplication: RefreshApplication)
    fun inject(appsFragment: AppsFragment)
    fun inject(mainActivity: MainActivity)
    fun inject(refreshService: RefreshService)
}