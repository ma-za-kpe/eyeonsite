package com.maku.eyeonsite.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object UploadSyncScheduler {
    private const val PERIODIC_INTERVAL_MINUTES = 15L

    private val constraints =
        Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    fun schedulePeriodicSync(context: Context) {
        val request =
            PeriodicWorkRequestBuilder<PendingUploadSyncWorker>(PERIODIC_INTERVAL_MINUTES, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PendingUploadSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun triggerImmediateSync(context: Context) {
        val request =
            OneTimeWorkRequestBuilder<PendingUploadSyncWorker>()
                .setConstraints(constraints)
                .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "${PendingUploadSyncWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
