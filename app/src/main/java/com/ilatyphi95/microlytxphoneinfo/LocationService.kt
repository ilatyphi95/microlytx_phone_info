package com.ilatyphi95.microlytxphoneinfo

import android.Manifest
import android.app.Activity
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class LocationService(activity: Activity) {
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(activity)

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun addLocationListener(useLocation: (Location?) -> Unit) {
        fusedLocation.lastLocation.addOnSuccessListener { location ->
            useLocation(location)
        }
    }

    private fun getLocationRequest() : LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        return locationRequest
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getLocationUpdate(locationCallback: LocationCallback) {
        fusedLocation.requestLocationUpdates(getLocationRequest(), locationCallback, null)
    }

    fun removeLocationUpdate(locationCallback: LocationCallback) {
        fusedLocation.removeLocationUpdates(locationCallback)
    }
}