package org.wit.inventorymanager.ui.faveMaps

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.*
import android.widget.Switch
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.wit.inventorymanager.R
import org.wit.inventorymanager.adapters.BuildingListener
import org.wit.inventorymanager.adapters.MapAdapter
import org.wit.inventorymanager.databinding.FragmentFaveMapsBinding
import org.wit.inventorymanager.helpers.createLoader
import org.wit.inventorymanager.helpers.hideLoader
import org.wit.inventorymanager.helpers.showLoader
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.buildingList.BuildingListViewModel
import org.wit.inventorymanager.ui.maps.MapsViewModel


class FaveMapsFragment : Fragment(), BuildingListener {

    private var nFragBinding: FragmentFaveMapsBinding? = null
    private val fragBinding get() = nFragBinding!!
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
                viewLifecycleOwner
            ) { buildings ->
                buildings?.let {
                    render(buildings as ArrayList<BuildingModel>)
                    hideLoader(loader)
                }
            }
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loader = createLoader(requireActivity())
        nFragBinding = FragmentFaveMapsBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        nFragBinding!!.mrecycler.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFave) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        buildingListViewModel.observableBuildingList.observe(viewLifecycleOwner) { building ->
            building?.let {
                renderRecycle(building as ArrayList<BuildingModel>)
                hideLoader(loader)
                checkSwipeRefresh()
            }
        }
        setSwipeRefresh()
    }
    private fun renderRecycle(buildingList: ArrayList<BuildingModel>) {
        nFragBinding?.mrecycler?.adapter = MapAdapter(buildingList,this)
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
                            .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_lightloc))
                    )
                }
            }
        }
    }

    private fun renderAll(buildingList: ArrayList<BuildingModel>) {
        if (buildingList.isNotEmpty()) {
            mapsViewModel.map.clear()
            buildingList.forEach {
                if (it.uid == this.buildingListViewModel.liveFirebaseUser.value!!.uid && it.faved) {
                    //markerColour = BitmapDescriptorFactory.HUE_ORANGE
                    mapsViewModel.map.addMarker(
                        MarkerOptions().position(LatLng(it.lat.toDouble(), it.lng.toDouble()))
                            .title("${it.name} , ${it.county}")
                            .snippet("Number of Staff: ${it.staff}")
                            .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_lightloc))
                    )
                }
                else {
                    mapsViewModel.map.addMarker(
                        MarkerOptions().position(LatLng(it.lat.toDouble(), it.lng.toDouble()))
                            .title("${it.name} , ${it.county}")
                            .snippet("Number of Staff: ${it.staff}")
                            .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_darkloc))
                    )
                }


            }
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_map, menu)

        val item = menu.findItem(R.id.toggle) as MenuItem
        item.setActionView(R.layout.switch_item)
        val toggleBuilds: Switch = item.actionView.findViewById(R.id.switch_menu)
        toggleBuilds.isChecked = false

        toggleBuilds.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                buildingListViewModel.observableBuildingList.observe(
                    viewLifecycleOwner
                ) { buildings ->
                    buildings?.let {
                        renderAll(buildings as ArrayList<BuildingModel>)
                        hideLoader(loader)
                    }
                }
            }
            else{
                buildingListViewModel.observableBuildingList.observe(
                    viewLifecycleOwner
                ) { buildings ->
                    buildings?.let {
                        render(buildings as ArrayList<BuildingModel>)
                        hideLoader(loader)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader, "Downloading Buildings")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                buildingListViewModel.liveFirebaseUser.value = firebaseUser
                buildingListViewModel.load()
            }
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        nFragBinding = null
    }

    private fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {
            fragBinding.swiperefresh.isRefreshing = true
            showLoader(loader, "Downloading Buildings")
            if(buildingListViewModel.readOnly.value!!)
                buildingListViewModel.loadAll()
            else
                buildingListViewModel.load()
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    override fun onBuildingClick(building: BuildingModel) {
        val loc = LatLng(building.lat.toDouble(), building.lng.toDouble())
        mapsViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14f))
    }

    override fun onEditSwipe(building: BuildingModel) {
    }

    override fun onFave(building: BuildingModel) {
        if (building.faved){
            building.faved = false
            buildingListViewModel.update(building.uid, building.id, building)
            setSwipeRefresh()
        }
        else if (!building.faved){
            building.faved = true
            buildingListViewModel.update(building.uid, building.id, building)
            setSwipeRefresh()
        }
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFave) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


}