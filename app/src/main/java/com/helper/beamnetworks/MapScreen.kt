package com.helper.beamnetworks

import android.Manifest
import android.annotation.SuppressLint
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val mapView = remember { MapView(context) }

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        locationPermissionsState.launchMultiplePermissionRequest()
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = {
                        if (locationPermissionsState.allPermissionsGranted) {
                            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val currentLocation = GeoPoint(it.latitude, it.longitude)
                                    mapView.controller.setCenter(currentLocation)
                                    mapView.controller.setZoom(18.0)
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Find My Location")
                }

                if (selectedLocation != null) {
                    FloatingActionButton(onClick = {
                        selectedLocation?.let {
                            // Pass location back to previous screen
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("picked_location", "${it.latitude},${it.longitude}")
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm Location")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (locationPermissionsState.allPermissionsGranted) {
                MapPicker(mapView, onLocationSelected = {
                    selectedLocation = it
                })
            } else {
                // Handle permission denial
                // You could show a message to the user here
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapPicker(mapView: MapView, onLocationSelected: (GeoPoint) -> Unit) {
    var marker: Marker? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(17.0)
                // redsoil
                controller.setCenter(GeoPoint(-1.236348, 36.9263500))

                val mapEventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let {
                            if (marker == null) {
                                marker = Marker(mapView)
                                marker?.position = it
                                marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                mapView.overlays.add(marker)
                            } else {
                                marker?.position = it
                            }
                            mapView.invalidate()
                            onLocationSelected(it)
                        }
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        return false
                    }
                }
                val mapEventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(mapEventsReceiver)
                overlays.add(0, mapEventsOverlay)
            }
        }
    )
}