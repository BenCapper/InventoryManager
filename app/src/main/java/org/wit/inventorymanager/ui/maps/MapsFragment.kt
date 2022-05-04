package org.wit.inventorymanager.ui.maps

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.inventorymanager.R
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.building.BuildingFragment
import org.wit.inventorymanager.ui.building.BuildingFragmentArgs
import splitties.snackbar.snack


class MapsFragment : Fragment(), GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private val mapsViewModel: MapsViewModel by activityViewModels()
    private lateinit var action: NavDirections
    var lat = ""
    var lng = ""

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        mapsViewModel.map = googleMap
        mapsViewModel.map.isMyLocationEnabled = true
        mapsViewModel.currentLocation.observe(viewLifecycleOwner) {
            val loc = LatLng(
                mapsViewModel.currentLocation.value!!.latitude,
                mapsViewModel.currentLocation.value!!.longitude
            )
            val options = MarkerOptions()
                .title("Branch Location")
                .snippet("GPS : $loc")
                .draggable(true)
                .position(loc)
            mapsViewModel.map.addMarker(options)
            mapsViewModel.map.setOnMarkerDragListener(this)
            mapsViewModel.map.uiSettings.isZoomControlsEnabled = true
            mapsViewModel.map.uiSettings.isMyLocationButtonEnabled = true
            mapsViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14f))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
        }
    }

    override fun onMarkerDrag(p0: Marker) {

    }

    override fun onMarkerDragEnd(p0: Marker) {
        lat = p0.position.latitude.toString()
        lng = p0.position.longitude.toString()
        action = MapsFragmentDirections.actionMapsFragmentToBuildingFragment(lat,lng)

        requireView().snack("$lat $lng")
    }

    override fun onMarkerDragStart(p0: Marker) {

    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return true
    }
}