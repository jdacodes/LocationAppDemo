package com.jdacodes.mybasiclocationapp

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel(private val locationManager: LocationManager) : ViewModel() {

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    fun shareCurrentLocation() {
        viewModelScope.launch {
            try {
                val location = locationManager.getCurrentLocation()
                _currentLocation.value = location
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