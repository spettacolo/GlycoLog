package com.uni.glycolog

import android.app.Application
import com.uni.glycolog.data.AppDatabase
import com.uni.glycolog.data.MeasurementRepository
import com.uni.glycolog.util.NotificationHelper

class GlycoLogApp : Application() {

    // database e repository creati una sola volta (lazy) e condivisi da tutta l'app
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MeasurementRepository(database.measurementDao()) }

    override fun onCreate() {
        super.onCreate()
        // i canali di notifica vanno creati subito, prima di inviare qualsiasi notifica
        NotificationHelper.createNotificationChannels(this)
    }
}
