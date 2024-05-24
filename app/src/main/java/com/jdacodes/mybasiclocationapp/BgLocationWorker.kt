package com.jdacodes.mybasiclocationapp

import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.concurrent.TimeUnit

class BgLocationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    companion object {
        // unique name for the work
        val workName = "BgLocationWorker"
        private const val TAG = "BackgroundLocationWork"
    }

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)
    override suspend fun doWork(): Result {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure()
        }
        locationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token,
        ).addOnSuccessListener { location ->
            location?.let {
                Log.d(
                    TAG,
                    "Current Location = [lat : ${location.latitude}, lng : ${location.longitude}]",
                )
            }
        }

        // Schedule next work
        val nextRequest = OneTimeWorkRequestBuilder<BgLocationWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()
//        WorkManager.getInstance(applicationContext).enqueue(nextRequest)
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            nextRequest
        )

        return Result.success()
    }
}
