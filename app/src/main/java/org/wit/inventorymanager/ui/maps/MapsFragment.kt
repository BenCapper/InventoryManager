package org.wit.inventorymanager.ui.maps

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
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
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.building.BuildingViewModel
import timber.log.Timber


class MapsFragment : Fragment(), GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private val mapsViewModel: MapsViewModel by activityViewModels()
    private val args by navArgs<MapsFragmentArgs>()
    private val buildingViewModel: BuildingViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private lateinit var action: NavDirections
    var lat = ""
    private var lng = ""

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
                .icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
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

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onMarkerDrag(p0: Marker) {

    }

    override fun onMarkerDragEnd(p0: Marker) {
        lat = p0.position.latitude.toString()
        lng = p0.position.longitude.toString()

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

    /**
     * It checks if the user has dragged the marker, if not it uses the current location. It then
     * creates a new building object with the new location and adds it to the database. It then
     * navigates back to the building list
     *
     * @param item MenuItem - The menu item that was selected.
     * @return The superclass method is being returned.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.item_confirm){
            if (lat == "" || lng == ""){
                lat = mapsViewModel.currentLocation.value!!.latitude.toString()
                lng = mapsViewModel.currentLocation.value!!.longitude.toString()


            }
            val build = BuildingModel(id=args.building.id, uid=args.building.uid,name=args.building.name, phone = args.building.phone, hiring = args.building.hiring, town=args.building.town,
                                                    county=args.building.county, staff=args.building.staff, lat =lat,lng=lng , faved = false)
            buildingViewModel.addBuilding(loggedInViewModel.liveFirebaseUser, build)
            action = MapsFragmentDirections.actionMapsFragmentToBuildingListFragment()
            Timber.i("BUILD BEING SENT: $build")
            findNavController().navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }
}