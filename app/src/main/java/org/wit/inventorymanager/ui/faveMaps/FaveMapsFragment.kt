package org.wit.inventorymanager.ui.faveMaps

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.inventorymanager.R
import org.wit.inventorymanager.helpers.createLoader
import org.wit.inventorymanager.helpers.hideLoader
import org.wit.inventorymanager.helpers.showLoader
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.buildingList.BuildingListViewModel
import org.wit.inventorymanager.ui.maps.MapsViewModel

class FaveMapsFragment : Fragment() {


    private val mapsViewModel: MapsViewModel by activityViewModels()
    private val buildingListViewModel: BuildingListViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    lateinit var loader : AlertDialog

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        mapsViewModel.map = googleMap
        mapsViewModel.map.isMyLocationEnabled = true
        mapsViewModel.currentLocation.observe(viewLifecycleOwner) {
            val loc = LatLng(
                mapsViewModel.currentLocation.value!!.latitude,
                mapsViewModel.currentLocation.value!!.longitude
            )

            mapsViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14f))
            mapsViewModel.map.uiSettings.isZoomControlsEnabled = true
            mapsViewModel.map.uiSettings.isMyLocationButtonEnabled = true

            buildingListViewModel.observableBuildingList.observe(
                viewLifecycleOwner,
                Observer { buildings ->
                    buildings?.let {
                        render(buildings as ArrayList<BuildingModel>)
                        hideLoader(loader)
                    }
                })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loader = createLoader(requireActivity())

        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun render(buildingList: ArrayList<BuildingModel>) {
        if (buildingList.isNotEmpty()) {
            mapsViewModel.map.clear()
            buildingList.forEach {
                if(it.faved) {
                    mapsViewModel.map.addMarker(
                        MarkerOptions().position(LatLng(it.lat.toDouble(), it.lng.toDouble()))
                            .title("${it.name} , ${it.county}")
                            .snippet("Number of Staff: ${it.staff}")
                            .icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                            )
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader, "Downloading Buildings")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                buildingListViewModel.liveFirebaseUser.value = firebaseUser
                buildingListViewModel.load()
            }
        })
    }
}