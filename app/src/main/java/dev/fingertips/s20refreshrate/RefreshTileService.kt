package dev.fingertips.s20refreshrate

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import d
import dev.fingertips.s20refreshrate.db.App
import dev.fingertips.s20refreshrate.db.AppDao
import dev.fingertips.s20refreshrate.db.Mode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@TargetApi(24)
class RefreshTileService : TileService(), CoroutineScope{
    @Inject lateinit var appDao: AppDao
    @Inject lateinit var context: Context
    @Inject lateinit var refreshRate: RefreshRate

    private val job = SupervisorJob()

    private var listening = false

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    init {
        RefreshApplication.appComponent.inject(this)
    }

    override fun onClick() {
        super.onClick()

        refreshRate.cycle()
        updateQsTile(fromClick = true)
    }

    override fun onStartListening() {
        super.onStartListening()
        listening = true

        updateQsTile()
    }

    private fun updateQsTile(fromClick: Boolean = false) {
        with (qsTile) {
            val mode: Mode

            this.state = Tile.STATE_INACTIVE

            this.label = refreshRate.lastRunningAppName

            when (refreshRate.peakRefreshRate) {
                60F -> {
                    this.icon = Icon.createWithResource(context, R.drawable.ic_qs_60)
                    // this.label = context.getString(R.string.sixty_hz)
                    mode = Mode.SIXTY
                }
                120F -> {
                    this.icon = Icon.createWithResource(context, R.drawable.ic_qs_120)
                    // this.label = context.getString(R.string.one_twenty_hz)
                    mode = Mode.ONE_TWENTY
                }
                else -> {
                    mode = Mode.DEFAULT
                }
            }

            if (fromClick) {
                d { refreshRate.lastRunningPackage }
                launch {
                    appDao.updateApp(App(refreshRate.lastRunningPackage, mode))
                }
            }
            this.updateTile()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        listening = false
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}