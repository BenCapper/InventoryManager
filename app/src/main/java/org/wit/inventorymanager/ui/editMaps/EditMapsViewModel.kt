package org.wit.inventorymanager.ui.editMaps

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import org.wit.inventorymanager.models.BuildingManager
import org.wit.inventorymanager.models.BuildingModel
import timber.log.Timber
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.RuntimeException

@SuppressLint("MissingPermission")
class EditMapsViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var map : GoogleMap
    var currentLocation = MutableLiveData<Location>()
    var locationClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)
    private val locationRequest = LocationRequest.create().apply {
        //interval = 10000
        //fastestInterval = 5000
        //priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            currentLocation.value = locationResult.locations.last()
        }
    }

    init {
        locationClient.requestLocationUpdates(locationRequest, locationCallback,
            Looper.getMainLooper())
    }

    fun updateCurrentLocation() {
        if(locationClient.lastLocation.isSuccessful)
            locationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    currentLocation.value = location!!
                }
        Timber.i("LOC : %s", currentLocation.value)
    }
}