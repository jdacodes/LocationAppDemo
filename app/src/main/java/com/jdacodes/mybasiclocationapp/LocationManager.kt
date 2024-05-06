package com.jdacodes.mybasiclocationapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationManager(private val context: Context) : ViewModelProvider.Factory {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Create a LocationRequest object using the LocationRequest.Builder class.
    val locationRequest = LocationRequest.Builder(
        10000
    ) // Set the desired interval for location updates to 10 seconds.
        .setMinUpdateIntervalMillis(5000) // Set the fastest interval for location updates to 5 seconds.
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY) // Set the priority of the location request to high accuracy.
        .build() // Build the LocationRequest object.
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // Handle the location updates.
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getCurrentLocation(): Location? {
        return if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            suspendCancellableCoroutine { continuation ->
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    continuation.resume(location)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
            }
        } else {
            null
        }
    }
}