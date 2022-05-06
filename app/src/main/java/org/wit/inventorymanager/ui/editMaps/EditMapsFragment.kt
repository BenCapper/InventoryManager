package org.wit.inventorymanager.ui.editMaps

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.inventorymanager.R
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.building.BuildingFragment
import org.wit.inventorymanager.ui.buildingDetail.BuildingDetailViewModel
import org.wit.inventorymanager.ui.maps.MapsFragmentArgs
import org.wit.inventorymanager.ui.maps.MapsFragmentDirections
import org.wit.inventorymanager.ui.maps.MapsViewModel
import splitties.snackbar.snack
import timber.log.Timber

class EditMapsFragment : Fragment(), GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private val mapsViewModel: MapsViewModel by activityViewModels()
    private val args by navArgs<MapsFragmentArgs>()
    private val buildingDetailViewModel: BuildingDetailViewModel by activityViewModels()
    private lateinit var action: NavDirections
    var lat = ""
    var lng = ""
    var latUpdate = ""
    var lngUpdate = ""

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        mapsViewModel.map = googleMap
        mapsViewModel.map.isMyLocationEnabled = true
        mapsViewModel.currentLocation.observe(viewLifecycleOwner) {
            val loc = LatLng(
                mapsViewModel.currentLocation.value!!.latitude,
                mapsViewModel.currentLocation.value!!.longitude
            )
            lat = args.building.lat
            lng = args.building.lng
            var currentLocation: LatLng = if (lat.isNullOrEmpty() || lng.isNullOrEmpty()){
                LatLng(52.2461,-7.1387)
            } else{
                LatLng(args.building.lat.toDouble(), args.building.lng.toDouble())
            }

            val options = MarkerOptions()
                .title("Branch Location")
                .snippet("GPS : $currentLocation")
                .draggable(true)
                .icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
                .position(currentLocation)
            mapsViewModel.map.addMarker(options)
            mapsViewModel.map.setOnMarkerDragListener(this)
            mapsViewModel.map.uiSettings.isZoomControlsEnabled = true
            mapsViewModel.map.uiSettings.isMyLocationButtonEnabled = true
            mapsViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14f))

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

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onMarkerDrag(p0: Marker) {

    }

    override fun onMarkerDragEnd(p0: Marker) {
        latUpdate = p0.position.latitude.toString()
        lngUpdate = p0.position.longitude.toString()

    }

    override fun onMarkerDragStart(p0: Marker) {

    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_confirm, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.item_confirm){
            if (latUpdate == "" || lngUpdate == ""){
                latUpdate = mapsViewModel.currentLocation.value!!.latitude.toString()
                lngUpdate = mapsViewModel.currentLocation.value!!.longitude.toString()


            }
            var build = BuildingModel(id=args.building.id, uid=args.building.uid,name=args.building.name, phone = args.building.phone, hiring = args.building.hiring, town=args.building.town,
                county=args.building.county, staff=args.building.staff, lat =latUpdate,lng=lngUpdate )
            buildingDetailViewModel.updateBuild(args.building.uid, args.building.id, build)
            action = EditMapsFragmentDirections.actionEditMapsFragmentToBuildingDetailFragment(build)
            Timber.i("BUILD BEING SENT: $build")
            findNavController().navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }
}

