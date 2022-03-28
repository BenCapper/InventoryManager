package org.wit.inventorymanager.ui.maps

import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
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
import splitties.snackbar.snack


class MapsFragment : Fragment(), GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private var build = BuildingModel()
    private var lat = (0).toDouble()
    private var lng = (0).toDouble()
    private var zoom = 0F


    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        val loc = LatLng(build.lat, build.lng)
        val options = MarkerOptions()
            .title("Branch Location")
            .snippet("GPS : $loc")
            .draggable(true)
            .position(loc)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, build.zoom))
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
        val action = MapsFragmentDirections.actionMapsFragmentToBuildingFragment(lat.toString(), lng.toString(), zoom)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val loc = LatLng(lat, lng)
        marker.snippet = "GPS : $loc"
        return false
    }


    override fun onMarkerDragStart(marker: Marker) {
    }

    override fun onMarkerDrag(p0: Marker) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val frag = BuildingFragment.newInstance()
        frag.arguments?.putParcelable("editBuild",build)
        requireActivity().supportFragmentManager.findFragmentById(R.id.buildingFragment)
            ?.let { requireActivity().supportFragmentManager.beginTransaction().remove(it)
                .replace(R.id.nav_host_fragment, frag).disallowAddToBackStack().commit() }
        return super.onOptionsItemSelected(item)
    }


    override fun onMarkerDragEnd(marker: Marker) {
        lat = marker.position.latitude
        lng = marker.position.longitude
        zoom = map.cameraPosition.zoom
        view?.snack(R.string.b_setLoc)
    }


}