package org.wit.inventorymanager.fragments

import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
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
import timber.log.Timber

class MapsFragment : Fragment(), GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    var location = Location()
    val action = MapsFragmentDirections.actionMapsFragmentToBuildingFragment()
    var uri = ""


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
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        val bundle = arguments
        location.zoom = bundle?.getFloat("loc")!!
        location.lng = bundle.getDouble("lng")
        location.lat = bundle.getDouble("lat")
        uri = bundle.getString("uri")!!
        toast(location.lng.toString())
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val loc = LatLng(location.lat, location.lng)
        marker.snippet = "GPS : $loc"
        return false
    }


    override fun onMarkerDragStart(marker: Marker) {
    }

    override fun onMarkerDrag(p0: Marker) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
        return super.onOptionsItemSelected(item)
    }


    override fun onMarkerDragEnd(marker: Marker) {
        location.lat = marker.position.latitude
        location.lng = marker.position.longitude
        location.zoom = map.cameraPosition.zoom
        action.arguments.putFloat("loc", location.zoom)
        action.arguments.putDouble("lat", location.lat)
        action.arguments.putDouble("lng", location.lng)
        action.arguments.putString("uri", uri)
        Timber.i(arguments.toString())
        Timber.i(action.toString())
    }


}