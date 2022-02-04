package org.wit.inventorymanager.fragments

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.inventorymanager.R
import org.wit.inventorymanager.models.Location
import splitties.toast.toast

class MapsFragment : Fragment(), GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    var location = Location()

    private val callback = OnMapReadyCallback { googleMap ->

        map = googleMap
        val loc = LatLng(location.lat, location.lng)
        val options = MarkerOptions()
            .title("Branch Location")
            .snippet("GPS : $loc")
            .draggable(true)
            .position(loc)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, location.zoom))
        map.setOnMarkerClickListener(this)
        map.addMarker(options)
        map.setOnMarkerDragListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bundle = arguments
        location.zoom = bundle?.getFloat("loc")!!
        location.lng = bundle?.getDouble("lng")
        location.lat = bundle?.getDouble("lat")
        toast(location.lng.toString())
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)


    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val loc = LatLng(location.lat, location.lng)
        marker.snippet = "GPS : $loc"
        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("loc", location)
        super.onSaveInstanceState(outState)
    }

    override fun onMarkerDragStart(marker: Marker) {
    }

    override fun onMarkerDrag(p0: Marker) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val action = MapsFragmentDirections.actionMapsFragmentToBuildingFragment()

        action.arguments.putFloat("loc", location.zoom)
        action.arguments.putDouble("lat", location.lat)
        action.arguments.putDouble("lng", location.lng)
        findNavController().navigate(action)
        return super.onOptionsItemSelected(item)
    }

    override fun onMarkerDragEnd(marker: Marker) {
        location.lat = marker.position.latitude
        location.lng = marker.position.longitude
        location.zoom = map.cameraPosition.zoom
    }

}