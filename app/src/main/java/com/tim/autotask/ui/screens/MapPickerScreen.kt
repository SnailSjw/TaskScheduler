package com.tim.autotask.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLat: Double,
    initialLng: Double,
    onConfirm: (MapPickerResult) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedLatLng by remember { mutableStateOf(LatLng(initialLat, initialLng)) }
    var selectedAddress by remember { mutableStateOf("") }
    var currentMarker by remember { mutableStateOf<com.amap.api.maps.model.Marker?>(null) }

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    val aMap = remember { mapView.map }

    LaunchedEffect(Unit) {
        aMap.uiSettings.isZoomControlsEnabled = true
        aMap.uiSettings.isMyLocationButtonEnabled = true

        val myLocationStyle = MyLocationStyle().apply {
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
            interval(5000)
            showMyLocation(true)
        }
        aMap.myLocationStyle = myLocationStyle
        aMap.isMyLocationEnabled = hasLocationPermission

        aMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(initialLat, initialLng), 15f)
        )

        addMarkerAtPosition(aMap, LatLng(initialLat, initialLng)).also {
            currentMarker = it
        }

        reverseGeocode(context, LatLonPoint(initialLat, initialLng)) { address ->
            selectedAddress = address
        }

        aMap.setOnMapClickListener { latLng ->
            currentMarker?.remove()
            val marker = addMarkerAtPosition(aMap, latLng)
            currentMarker = marker
            selectedLatLng = latLng

            reverseGeocode(context, LatLonPoint(latLng.latitude, latLng.longitude)) { address ->
                selectedAddress = address
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onDestroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择位置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索地点...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (searchQuery.isNotBlank()) {
                        searchPoi(context, searchQuery, selectedLatLng) { poi ->
                            val latLng = LatLng(poi.latLonPoint.latitude, poi.latLonPoint.longitude)
                            currentMarker?.remove()
                            currentMarker = addMarkerAtPosition(aMap, latLng)
                            selectedLatLng = latLng
                            selectedAddress = poi.title
                            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                        }
                    }
                }) {
                    Text("搜索")
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (selectedAddress.isNotBlank()) {
                        Text(
                            text = selectedAddress,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "坐标：(${String.format("%.6f", selectedLatLng.latitude)}, ${String.format("%.6f", selectedLatLng.longitude)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                MapPickerResult(
                                    latitude = selectedLatLng.latitude,
                                    longitude = selectedLatLng.longitude,
                                    address = selectedAddress.ifBlank { "(${String.format("%.4f", selectedLatLng.latitude)}, ${String.format("%.4f", selectedLatLng.longitude)})" }
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("确认选择此位置")
                    }
                }
            }
        }
    }
}

private fun addMarkerAtPosition(aMap: AMap, latLng: LatLng): com.amap.api.maps.model.Marker {
    val markerOptions = MarkerOptions()
        .position(latLng)
        .title("选中位置")
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
    return aMap.addMarker(markerOptions)
}

private fun reverseGeocode(
    context: android.content.Context,
    point: LatLonPoint,
    onResult: (String) -> Unit
) {
    try {
        val search = GeocodeSearch(context)
        search.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onRegeocodeSearched(result: RegeocodeResult?, code: Int) {
                if (code == 1000 && result != null) {
                    onResult(result.regeocodeAddress.formatAddress)
                }
            }

            override fun onGeocodeSearched(result: GeocodeResult?, code: Int) {}
        })
        val query = RegeocodeQuery(point, 200f, GeocodeSearch.AMAP)
        search.getFromLocationAsyn(query)
    } catch (_: Exception) {}
}

private fun searchPoi(
    context: android.content.Context,
    keyword: String,
    center: LatLng,
    onResult: (PoiItem) -> Unit
) {
    try {
        val searchQuery = PoiSearch.Query(keyword, "", "")
        searchQuery.pageSize = 1
        searchQuery.pageNum = 0
        val search = PoiSearch(context, searchQuery)
        search.bound = PoiSearch.SearchBound(
            LatLonPoint(center.latitude, center.longitude),
            5000
        )
        search.setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
            override fun onPoiSearched(result: PoiResult?, code: Int) {
                if (code == 1000 && result != null && result.pois.isNotEmpty()) {
                    onResult(result.pois[0])
                }
            }

            override fun onPoiItemSearched(item: PoiItem?, code: Int) {}
        })
        search.searchPOIAsyn()
    } catch (_: Exception) {}
}
