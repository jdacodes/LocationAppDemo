package com.jdacodes.mybasiclocationapp

import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel(private val locationManager: LocationManager) : ViewModel() {

    private val _lastKnownLocation = MutableStateFlow<Location?>(null)
    val lastKnownLocation: StateFlow<Location?> = _lastKnownLocation.asStateFlow()

    private val _locationData = MutableStateFlow<LocationData?>(null)
    val locationData: StateFlow<LocationData?> = _locationData.asStateFlow()

    var checkLocationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    private val _showDialog = MutableStateFlow<Boolean>(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()
//    var showDialog = mutableStateOf(false)
//    var dialogMessage = mutableStateOf("")
    private val _dialogMessage = MutableStateFlow<String>("")
    val dialogMessage: StateFlow<String> = _dialogMessage.asStateFlow()

    fun getLocationData() {
        // Emit the LocationData object to the StateFlow
        _locationData.value = locationManager.locationData
        locationData.value?.let {
           Log.d("LocationViewModel", "Location data: $it")
        }
    }

    fun getGoogleMapsIntentData(latitude: Double, longitude: Double): Pair<Uri, String?> {
        val uri = Uri.parse("geo:$latitude,$longitude?z=18")
        val packageName = "com.google.android.apps.maps"
        return Pair(uri, packageName)
    }

    fun shareLastKnownLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val location = locationManager.getLastKnownLocation()
                _lastKnownLocation.value = location
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun checkLocationSettings() {
        locationManager.checkLocationSettings(
            builder = locationManager.getCurrentLocationSettingsBuilder(),
            onSuccess = {
                // Handle success
                Log.d("LocationViewModel", "Check Location Settings accepted")
                _dialogMessage.value = "Location settings are satisfied."
                _showDialog.value = true
            },
            onFailure = { pendingIntent ->
                pendingIntent?.let {
                    Log.d("LocationViewModel", "Check Location Settings denied")
                    val intentSenderRequest = IntentSenderRequest.Builder(it).build()
                    checkLocationSettingsLauncher?.launch(intentSenderRequest)
                } ?: run {
                    _dialogMessage.value = "Location settings are not satisfied and cannot be resolved."
                    _showDialog.value = true
                }
            }
        )
    }
    fun onLocationSettingsChangeAccepted() {
        // Handle location settings accepted
        _dialogMessage.value = "Location settings change accepted."
        _showDialog.value = true

    }

    fun onLocationSettingsChangeDenied() {
        // Handle location settings denied
        _dialogMessage.value = "Location settings change denied."
        _showDialog.value = true

    }

    fun dismissDialog() {
        _showDialog.value = false
    }
}

class LocationViewModelFactory(private val locationManager: LocationManager) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(locationManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}