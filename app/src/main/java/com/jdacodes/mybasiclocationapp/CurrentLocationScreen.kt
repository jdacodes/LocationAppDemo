package com.jdacodes.mybasiclocationapp

import androidx.compose.runtime.Composable
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.getValue


@SuppressLint("MissingPermission")
@Composable
fun CurrentLocationScreen(
    drawerState: DrawerState,
    viewModel: LocationViewModel,
) {
    val permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    Scaffold(
        topBar = { CustomAppBar(
            drawerState = drawerState,
            title = "Current Location"
        ) }
    ) { paddingValues ->
        PermissionBox(
            permissions = permissions,
            requiredPermissions = listOf(permissions.first()),
            onGranted = {
                CurrentLocationContent(
                    usePreciseLocation = it.contains(Manifest.permission.ACCESS_FINE_LOCATION),
                    viewModel,
                    paddingValues
                )
            },
        )
    }
}

@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun CurrentLocationContent(
    usePreciseLocation: Boolean,
    viewModel: LocationViewModel,
    paddingValues: PaddingValues
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var locationInfo by remember {
        mutableStateOf("")
    }
    var latitude by remember {
        mutableDoubleStateOf(0.0)
    }
    var longitude by remember {
        mutableDoubleStateOf(0.0)
    }

    val showDialog by
    viewModel.showDialog.collectAsState()

    val dialogMessage by viewModel.dialogMessage.collectAsState()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            confirmButton = {
                Button(onClick = { viewModel.dismissDialog() }) {
                    Text("OK")
                }
            },
            text = { Text(dialogMessage) }
        )
    }

        Column(
            Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = {
                    // getting last known location is faster and minimizes battery usage
                    // This information may be out of date.
                    // Location may be null as previously no client has access location
                    // or location turned of in device setting.
                    // Please handle for null case as well as additional check can be added before using the method
                    scope.launch(Dispatchers.IO) {
                        val result = locationClient.lastLocation.await()
                        locationInfo = if (result == null) {
                            "No last known location. Try fetching the current location first"
                        } else {
                            "Current location is \n" + "lat : ${result.latitude}\n" +
                                    "long : ${result.longitude}\n" + "fetched at ${System.currentTimeMillis()}"
                        }

                        result?.let { fetchedLocation ->
                            latitude = fetchedLocation.latitude
                            longitude = fetchedLocation.longitude


                        }
                    }
                },
            ) {
                Text("Get last known location")
            }

            Button(
                onClick = {
                    //To get more accurate or fresher device location use this method
                    scope.launch(Dispatchers.IO) {
                        val priority = if (usePreciseLocation) {
                            Priority.PRIORITY_HIGH_ACCURACY
                        } else {
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY
                        }
                        val result = locationClient.getCurrentLocation(
                            priority,
                            CancellationTokenSource().token,
                        ).await()
                        result?.let { fetchedLocation ->
                            locationInfo =
                                "Current location is \n" + "lat : ${fetchedLocation.latitude}\n" +
                                        "long : ${fetchedLocation.longitude}\n" + "fetched at ${System.currentTimeMillis()}"
                            latitude = fetchedLocation.latitude
                            longitude = fetchedLocation.longitude
                        }
                    }
                },
            ) {
                Text(text = "Get current location")
            }
            Text(
                text = locationInfo,
            )
            Button(
                onClick = {
                    val (uri, packageName) = viewModel.getGoogleMapsIntentData(latitude, longitude)
                    launchGoogleMaps(context, uri, packageName)
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Open Google Maps")
            }
            Button(onClick = { viewModel.checkLocationSettings() }) {
                Text("Check Location Settings")
            }
        }
}

fun launchGoogleMaps(context: Context, uri: Uri, packageName: String?) {
    // Create a Uri from an intent string. Open map using intent to pin a specific location (latitude, longitude)

    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage(packageName)
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Log.d("launchGoogleMaps", "Package manager is null, launching maps")
        context.startActivity(intent)
    }


}