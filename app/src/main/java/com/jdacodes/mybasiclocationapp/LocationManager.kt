package com.jdacodes.mybasiclocationapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationManager(private val context: Context) : ViewModelProvider.Factory {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Create a LocationRequest object using the LocationRequest.Builder class.
    val locationRequest = LocationRequest.Builder(
        0
    )
        .setIntervalMillis(10000)// Set the desired interval for location updates to 10 seconds.
        .setMinUpdateIntervalMillis(5000) // Set the fastest interval for location updates to 5 seconds.
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY) // Set the priority of the location request to high accuracy.
        .build() // Build the LocationRequest object.

//    var builder = getCurrentLocationSettingsBuilder()
//    val locationSettings = checkLocationSettings(builder)

    fun getCurrentLocationSettingsBuilder(): LocationSettingsRequest.Builder {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest) //Get current location settings of user's device
        return builder
    }

    fun checkLocationSettings(
        builder: LocationSettingsRequest.Builder,
        onSuccess: () -> Unit,
        onFailure: (PendingIntent?) -> Unit
    ) {
        //Check whether the current location settings are satisfied or not.
        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
            .addOnSuccessListener { locationSettingsResponse ->
                // All location settings are satisfied. The client can initialize location requests here.
                //location requests here.

                //Verify the location settings states.
                val state =
                locationSettingsResponse.locationSettingsStates?.isLocationUsable //true or false

                if (state == true) {
                    //update the ui here
                    onSuccess()
                }

            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        val REQUEST_CHECK_SETTINGS = 101
                        exception.startResolutionForResult(
                            context as MainActivity,
                            REQUEST_CHECK_SETTINGS
                        )
                        onFailure(exception.resolution)

                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                        onFailure(null)
                    }

                }
            }
    }

    //Check whether the current location settings are satisfied or not.
//    val client: SettingsClient = LocationServices.getSettingsClient(context)
//    val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
//        .addOnSuccessListener { locationSettingsResponse ->
//            // All location settings are satisfied. The client can initialize location requests here.
//            //location requests here.
//
//            //Verify the location settings states.
//            locationSettingsResponse.locationSettingsStates?.isGpsUsable //true or false
//            locationSettingsResponse.locationSettingsStates?.isLocationUsable //true or false
//
//        }
//        .addOnFailureListener { exception ->
//            if (exception is ResolvableApiException) {
//                // Location settings are not satisfied, but this can be fixed
//                // by showing the user a dialog.
//                try {
//                    // Show the dialog by calling startResolutionForResult(),
//                    // and check the result in onActivityResult().
//                    val REQUEST_CHECK_SETTINGS = 101
//                    exception.startResolutionForResult(
//                        context as MainActivity,
//                        REQUEST_CHECK_SETTINGS
//                    )
//
//                } catch (sendEx: IntentSender.SendIntentException) {
//                    // Ignore the error.
//                }
//
//            }
//        }

    lateinit var locationData: LocationData
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult ?: return
            for (location in locationResult.locations) {
                // Update UI with location data
                // ...
                // Create a LocationData object from the location data
                locationData = LocationData(
                    latitude = location.latitude.toString(),
                    longitude = location.longitude.toString(),
                    accuracy = location.accuracy.toString(),
                    bearing = location.bearing.toString(),
                    speed = location.speed.toString(),
                    time = location.time.toString(),
                    altitude = location.altitude.toString()
                )


            }

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
    suspend fun getLastKnownLocation(): Location? {
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