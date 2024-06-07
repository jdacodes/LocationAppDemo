package com.jdacodes.mybasiclocationapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MainNavigation(
    viewModel: LocationViewModel,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
) {

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(menus) { route ->
                    coroutineScope.launch {
                        drawerState.close()
                    }

                    navController.navigate(route)
                }
            }
        }
    ) {
        NavHost(navController = navController, startDestination = MainRoute.CurrentLocation.name) {
            composable(MainRoute.CurrentLocation.name) {
                CurrentLocationScreen(drawerState, viewModel)
            }

            composable(MainRoute.LocationUpdates.name) {
                LocationUpdatesScreen(drawerState)
            }
            composable(MainRoute.BgLocationAccess.name) {
                BgLocationAccessScreen(drawerState)
            }
            composable(MainRoute.LocationPermission.name) {
                LocationPermissionScreen(
                    drawerState = drawerState,
                    onShareLocation = viewModel::shareLastKnownLocation,
                    onLocationUpdate = viewModel::getLocationData,
                    currentLocation = viewModel.lastKnownLocation,
                    locationData = viewModel.locationData
                )
            }
        }
    }
}