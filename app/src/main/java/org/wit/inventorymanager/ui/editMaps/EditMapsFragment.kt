package org.wit.inventorymanager.ui.editMaps

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
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
import org.wit.inventorymanager.ui.maps.MapsFragmentDirections
import splitties.snackbar.snack

class EditMapsFragment : Fragment(), GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private var build = BuildingModel()


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
        val bundle = arguments
        build = bundle?.getParcelable("build")!!
        val action = MapsFragmentDirections.actionMapsFragmentToBuildingFragment()
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            action.arguments.putParcelable("editBuild", build)
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val loc = LatLng(build.lat, build.lng)
        marker.snippet = "GPS : $loc"
        return false
    }


    override fun onMarkerDragStart(marker: Marker) {
    }

    override fun onMarkerDrag(p0: Marker) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val frag = BuildingFragment.newInstance()
        frag.arguments?.putParcelable("editBuild", build)
        requireActivity().supportFragmentManager.findFragmentById(R.id.buildingFragment)
            ?.let {
                requireActivity().supportFragmentManager.beginTransaction().remove(it)
                    .replace(R.id.nav_host_fragment, frag).disallowAddToBackStack().commit()
            }
        return super.onOptionsItemSelected(item)
    }


    override fun onMarkerDragEnd(marker: Marker) {
        build.lat = marker.position.latitude
        build.lng = marker.position.longitude
        build.zoom = map.cameraPosition.zoom
        view?.snack(R.string.b_setLoc)
    }
}

