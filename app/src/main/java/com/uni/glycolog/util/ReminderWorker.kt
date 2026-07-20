package com.uni.glycolog.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

// promemoria terapeutico periodico: WorkManager e' la soluzione consigliata a lezione
// per i task differiti/periodici (al posto dei Service in background)
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        NotificationHelper.showReminder(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "glycolog_reminder"

        // preferenze condivise per ricordare le impostazioni del promemoria
        const val PREFS_NAME = "glycolog_prefs"
        const val KEY_ENABLED = "reminder_enabled"
        const val KEY_HOURS = "reminder_hours"
        const val DEFAULT_HOURS = 8

        // pianifica (o ripianifica) il promemoria periodico
        fun schedule(context: Context, hours: Int) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(hours.toLong(), TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
