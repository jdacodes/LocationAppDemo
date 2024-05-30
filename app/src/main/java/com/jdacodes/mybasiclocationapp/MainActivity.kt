package com.jdacodes.mybasiclocationapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jdacodes.mybasiclocationapp.ui.theme.MyBasicLocationAppTheme
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val viewModel by viewModels<LocationViewModel> { LocationManager(this) }
        locationManager = LocationManager(this)
        val viewModelFactory = LocationViewModelFactory(locationManager)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(LocationViewModel::class.java)

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.

                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                }

                else -> {
                    // No location access granted.
                }
            }

        }

        setContent {
            MyBasicLocationAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //Requesting permissions at runtime on Activity creation for convenience purposes

                    // Before you perform the actual permission request, check whether your app
                    // already has the permissions, and whether your app needs to show a permission
                    // rationale dialog. For more details, see Request permissions.
                    when {

                        checkLocationPermissions() -> {
                            // You can use the API that requires the permission.
                        }

                        // In an educational UI, explain to the user why your app requires this
                        // permission for a specific feature to behave as expected, and what
                        // features are disabled if it's declined. In this UI, include a
                        // "cancel" or "no thanks" button that lets the user continue
                        // using your app without granting the permission.
                        showLocationPermissionRationale() -> {
                            ShowLocationPermissionRationaleDialog(
                                onGrantPermission = {
                                    locationPermissionRequest.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            )
                        }

                        else ->
                            // You can directly ask for the permission.
                            // The registered ActivityResultCallback gets the result of this request.

                            locationPermissionRequest.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                    }
//                    Greeting("Android")
                    val currentLocation = viewModel.lastKnownLocation
                    val locationData = viewModel.locationData
//                    LocationPermissionScreen(
//                        onShareLocation = viewModel::shareLastKnownLocation,
//                        onLocationUpdate = viewModel::getLocationData,
//                        currentLocation = currentLocation,
//                        locationData = locationData)

                    CurrentLocationScreen(viewModel)

//                    LocationUpdatesScreen()

//                    BgLocationAccessScreen()
                }
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationPermission && coarseLocationPermission
    }

    private fun showLocationPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                )
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun ShowLocationPermissionRationaleDialog(onGrantPermission: () -> Unit) {
        val openDialog = remember { mutableStateOf(true) }

        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    // Dismiss the dialog when the user clicks outside the dialog or on the back
                    // button. If you want to disable that functionality, simply use an empty
                    // onDismissRequest.
                    openDialog.value = false
                },
                title = {
                    Text(text = "Location Permission Needed")
                },
                text = {
                    Text(text = "This app needs both fine and coarse location to access your precise location for features like finding nearby places. Coarse location is used for a general idea of your area, while fine location provides more accurate details.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDialog.value = false
                            onGrantPermission()
                        }
                    ) {
                        Text("Grant Permission")

                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openDialog.value = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

    }

    override fun onResume() {
        super.onResume()
        locationManager.startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        locationManager.stopLocationUpdates()
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyBasicLocationAppTheme {
        Greeting("Android")
    }
}