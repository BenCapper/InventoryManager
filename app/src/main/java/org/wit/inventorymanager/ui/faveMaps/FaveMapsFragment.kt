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
    private lateinit var loader : AlertDialog


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
    /**
     * It renders the recycler view with the building list.
     *
     * @param buildingList ArrayList<BuildingModel>
     */
    private fun renderRecycle(buildingList: ArrayList<BuildingModel>) {
        nFragBinding?.mrecycler?.adapter = MapAdapter(buildingList,this)
    }

    /**
     * The function takes in an arraylist of building models, clears the map, and then adds a marker to
     * the map for each building in the arraylist
     *
     * @param buildingList ArrayList<BuildingModel>
     */
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

    /**
     * This function is called when the user clicks on the search button in the search bar. It takes
     * the user's input and searches the database for any buildings that match the user's input. If
     * there are any matches, the map is cleared and the markers for the buildings that match the
     * user's input are added to the map
     *
     * @param buildingList ArrayList<BuildingModel> - This is the list of buildings that we want to
     * render on the map.
     */
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

    /**
     * It takes a vector drawable and converts it into a bitmap
     *
     * @param context Context - The context of the activity.
     * @param vectorResId The resource ID of the vector drawable.
     * @return A BitmapDescriptor object.
     */
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
    /**
     * This function adds a switch to the menu bar. When the switch is checked, the renderAll function
     * is called. When the switch is not checked, the render function is called
     *
     * @param menu Menu - This is the menu that is being created.
     * @param inflater MenuInflater - This is the menu inflater that is used to inflate the menu.
     */
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

    /**
     * It sets the swipe refresh listener.
     */
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

    /**
     * It checks if the swipe refresh is refreshing and if it is, it sets it to false.
     */
    private fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    /**
     * When a building is clicked, move the camera to the building's location
     *
     * @param building BuildingModel - The building that was clicked
     */
    override fun onBuildingClick(building: BuildingModel) {
        val loc = LatLng(building.lat.toDouble(), building.lng.toDouble())
        mapsViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14f))
    }

    override fun onEditSwipe(building: BuildingModel) {
    }

    /**
     * The function is called when the user clicks the favorite button on the building card. If the
     * building is already favorited, it will unfavorite it. If the building is not favorited, it will
     * favorite it
     *
     * @param building BuildingModel - the building that was clicked on
     */
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