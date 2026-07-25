package com.linkit.company.feature.map.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
internal actual fun CurrentLocationEffect(
    requestToken: Int,
    onLocationAvailable: (MapCoordinateUiModel) -> Unit,
    onLocationUnavailable: () -> Unit,
) {
    val currentOnLocationAvailable by rememberUpdatedState(onLocationAvailable)
    val currentOnLocationUnavailable by rememberUpdatedState(onLocationUnavailable)

    if (LocalInspectionMode.current) {
        LaunchedEffect(requestToken) {
            if (requestToken > 0) currentOnLocationUnavailable()
        }
        return
    }

    val context = LocalContext.current
    val locationManager = remember(context) {
        context.getSystemService(LocationManager::class.java)
    }
    var activeRequest by remember { mutableStateOf<ActiveLocationRequest?>(null) }
    var permissionRequestInFlight by remember { mutableStateOf(false) }

    val startLocationLookup: () -> Unit = {
        activeRequest?.cancel()
        activeRequest = locationManager?.requestCurrentOrLastLocation(
            context = context,
            onLocationAvailable = currentOnLocationAvailable,
            onLocationUnavailable = currentOnLocationUnavailable,
        ) ?: ActiveLocationRequest.None.also { currentOnLocationUnavailable() }
    }
    val currentStartLocationLookup by rememberUpdatedState(startLocationLookup)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        permissionRequestInFlight = false
        val locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            context.hasLocationPermission()
        if (locationGranted) {
            currentStartLocationLookup()
        } else {
            currentOnLocationUnavailable()
        }
    }

    LaunchedEffect(requestToken) {
        if (requestToken <= 0) return@LaunchedEffect

        if (context.hasLocationPermission()) {
            currentStartLocationLookup()
        } else if (!permissionRequestInFlight) {
            permissionRequestInFlight = true
            permissionLauncher.launch(LocationPermissions)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activeRequest?.cancel()
            activeRequest = null
        }
    }
}

private val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

private fun Context.hasLocationPermission(): Boolean =
    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun Context.hasFineLocationPermission(): Boolean =
    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
private fun LocationManager.requestCurrentOrLastLocation(
    context: Context,
    onLocationAvailable: (MapCoordinateUiModel) -> Unit,
    onLocationUnavailable: () -> Unit,
): ActiveLocationRequest {
    if (!context.hasLocationPermission()) {
        onLocationUnavailable()
        return ActiveLocationRequest.None
    }

    val enabledProviders = runCatching { getProviders(true) }.getOrDefault(emptyList())
    val readableProviders = enabledProviders.filter { provider ->
        provider != LocationManager.GPS_PROVIDER || context.hasFineLocationPermission()
    }
    val lastLocation = readableProviders
        .mapNotNull { provider -> runCatching { getLastKnownLocation(provider) }.getOrNull() }
        .filter(Location::isUsable)
        .maxByOrNull(Location::getElapsedRealtimeNanos)

    if (lastLocation != null) {
        onLocationAvailable(lastLocation.toUiModel())
        return ActiveLocationRequest.None
    }

    val provider = when {
        context.hasFineLocationPermission() &&
            LocationManager.GPS_PROVIDER in readableProviders -> LocationManager.GPS_PROVIDER

        LocationManager.NETWORK_PROVIDER in readableProviders -> LocationManager.NETWORK_PROVIDER
        else -> readableProviders.firstOrNull { it != LocationManager.PASSIVE_PROVIDER }
    }
    if (provider == null) {
        onLocationUnavailable()
        return ActiveLocationRequest.None
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        requestCurrentLocationApi30(
            context = context,
            provider = provider,
            onLocationAvailable = onLocationAvailable,
            onLocationUnavailable = onLocationUnavailable,
        )
    } else {
        requestSingleUpdateLegacy(
            provider = provider,
            onLocationAvailable = onLocationAvailable,
            onLocationUnavailable = onLocationUnavailable,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("MissingPermission")
private fun LocationManager.requestCurrentLocationApi30(
    context: Context,
    provider: String,
    onLocationAvailable: (MapCoordinateUiModel) -> Unit,
    onLocationUnavailable: () -> Unit,
): ActiveLocationRequest {
    val cancellationSignal = CancellationSignal()
    var completed = false
    val request = ActiveLocationRequest {
        completed = true
        cancellationSignal.cancel()
    }
    runCatching {
        getCurrentLocation(provider, cancellationSignal, context.mainExecutor) { location ->
            if (completed) return@getCurrentLocation
            completed = true
            location
                ?.takeIf(Location::isUsable)
                ?.let { onLocationAvailable(it.toUiModel()) }
                ?: onLocationUnavailable()
        }
    }.onFailure {
        request.cancel()
        onLocationUnavailable()
    }
    return request
}

@SuppressLint("MissingPermission")
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
private fun LocationManager.requestSingleUpdateLegacy(
    provider: String,
    onLocationAvailable: (MapCoordinateUiModel) -> Unit,
    onLocationUnavailable: () -> Unit,
): ActiveLocationRequest {
    var completed = false
    val mainHandler = Handler(Looper.getMainLooper())
    lateinit var listener: LocationListener
    val timeout = Runnable {
        if (completed) return@Runnable
        completed = true
        runCatching { removeUpdates(listener) }
        onLocationUnavailable()
    }
    val request = ActiveLocationRequest {
        completed = true
        mainHandler.removeCallbacks(timeout)
        runCatching { removeUpdates(listener) }
    }
    listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (completed) return
            completed = true
            mainHandler.removeCallbacks(timeout)
            runCatching { removeUpdates(this) }
            location
                .takeIf(Location::isUsable)
                ?.let { onLocationAvailable(it.toUiModel()) }
                ?: onLocationUnavailable()
        }

        override fun onProviderDisabled(provider: String) {
            if (completed) return
            completed = true
            mainHandler.removeCallbacks(timeout)
            runCatching { removeUpdates(this) }
            onLocationUnavailable()
        }

        override fun onProviderEnabled(provider: String) = Unit

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }
    runCatching {
        requestSingleUpdate(provider, listener, Looper.getMainLooper())
        mainHandler.postDelayed(timeout, CurrentLocationTimeoutMillis)
    }.onFailure {
        request.cancel()
        onLocationUnavailable()
    }
    return request
}

private const val CurrentLocationTimeoutMillis = 20_000L

private fun Location.isUsable(): Boolean =
    latitude.isFinite() && longitude.isFinite() &&
        latitude in -90.0..90.0 && longitude in -180.0..180.0

private fun Location.toUiModel(): MapCoordinateUiModel = MapCoordinateUiModel(
    lat = latitude,
    lng = longitude,
)

private fun interface ActiveLocationRequest {
    fun cancel()

    data object None : ActiveLocationRequest {
        override fun cancel() = Unit
    }
}
