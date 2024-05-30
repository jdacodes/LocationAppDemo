package com.jdacodes.mybasiclocationapp

import android.location.Location
import android.net.Uri
import android.util.Log
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

    fun getLocationData() {
        // Emit the LocationData object to the StateFlow
        _locationData.value = locationManager.locationData
        locationData.value?.let {
           Log.d("LocationViewModel", "Location data: $it")
        }
    }

    fun getGoogleMapsIntentData(latitude: Double, longitude: Double): Pair<Uri, String?> {
        val uri = Uri.parse("geo:$latitude,$longitude")
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
}

class LocationViewModelFactory(private val locationManager: LocationManager) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(locationManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}