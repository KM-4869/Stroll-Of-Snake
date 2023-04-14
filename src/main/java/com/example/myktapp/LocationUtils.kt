package com.example.lbs

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.location.provider.ProviderProperties
import android.os.Build
import androidx.annotation.RequiresApi

class LocationUtils(
    private val context: Context
): LocationListener {
    private lateinit var locationManager: LocationManager

    private var onLocationValuesChanged: ((List<Double>, Long)-> Unit)? = null

    private var gpsProvider: LocationProvider? = null
    private var netProvider: LocationProvider? = null

    fun setOnLocationValuesChangedListener(listener: (List<Double>, Long) -> Unit) {
        onLocationValuesChanged = listener
    }

    override fun onLocationChanged(location: Location) {
        onLocationValuesChanged?.invoke(
            listOf(location.latitude, location.longitude, location.altitude),
            location.time
        )
    }

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (!context.hasLocationPermission()) {
            return
        }
        if(!::locationManager.isInitialized && gpsProvider == null && netProvider == null) {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER)
            netProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER)
        }
        if (gpsProvider != null) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                0.01f,
                this
            )
        } else if (netProvider != null) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                0.01f,
                this
            )
        } else {
            return
        }
    }

    fun stopListening() {
        if(!::locationManager.isInitialized) {
            return
        }
        locationManager.removeUpdates(this)
    }
}