package com.jdacodes.mybasiclocationapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn

val menus = arrayOf(
    DrawerMenu(Icons.Filled.LocationOn, "Current Location", MainRoute.CurrentLocation.name),
    DrawerMenu(Icons.Filled.LocationOn, "Location Updates", MainRoute.LocationUpdates.name),
    DrawerMenu(Icons.Filled.LocationOn, "Background Location", MainRoute.BgLocationAccess.name),
    DrawerMenu(Icons.Filled.LocationOn, "Location Permissions", MainRoute.LocationPermission.name)
)