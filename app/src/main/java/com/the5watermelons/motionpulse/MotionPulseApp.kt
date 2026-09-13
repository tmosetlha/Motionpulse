package com.the5watermelons.motionpulse

import android.app.Application
import com.the5watermelons.motionpulse.data.sync.SyncScheduler

class MotionPulseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Safety-net sync sweep: catches anything that was written offline
        // and never got picked up by an immediate retry (e.g. app was killed
        // before connectivity returned).
        SyncScheduler.schedulePeriodicSync(this)
    }
}