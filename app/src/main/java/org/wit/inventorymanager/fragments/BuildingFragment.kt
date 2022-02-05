package org.wit.inventorymanager.fragments


import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.widget.EditText
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.set
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingBinding
import org.wit.inventorymanager.main.InventoryApp
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.Location
import splitties.toast.toast
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

lateinit var app: InventoryApp


private var _fragBinding: FragmentBuildingBinding? = null
private val fragBinding get() = _fragBinding!!
private var building = BuildingModel()






class BuildingFragment : Fragment() {

    private var uri: android.net.Uri? =  null
    private var img = "null"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as InventoryApp
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentBuildingBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setButtonListener(fragBinding)
        activity?.title = getString(R.string.action_location)
        val bundle = arguments
        if (arguments?.isEmpty == false) {
            building.zoom = bundle?.getFloat("loc")!!
            building.lng = bundle?.getDouble("lng")!!
            building.lat = bundle?.getDouble("lat")!!
            //img = bundle?.getString("uri")!!


        }
        toast(building.lat.toString())

        if(building.image !== "null"){
            fragBinding.buildingImage.setImageURI(Uri.parse(building.image))
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
        requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_buildingFragment_to_buildingListFragment)
        }
        val selectPictureLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            fragBinding.buildingImage.setImageURI(it)
            uri = it
        }
        fragBinding.chooseImage.setOnClickListener{
            selectPictureLauncher.launch("image/*")
        }
        fragBinding.buildingLocation.setOnClickListener{
            val location = Location(52.245696, -7.139102, 15f)
            if (building.zoom != 0f) {
                location.lat =  building.lat
                location.lng = building.lng
                location.zoom = building.zoom
            }

            val action = BuildingFragmentDirections.actionBuildingFragmentToMapsFragment()

            action.arguments.putFloat("loc", location.zoom)
            action.arguments.putDouble("lat", location.lat)
            action.arguments.putDouble("lng", location.lng)
            action.arguments.putString("uri", building.image)
            //it.findNavController().navigate(action)
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
        }
        if (building.name !== ""){
            fragBinding.buildingName.setText(building.name)
        }
        if (building.address !== ""){
            fragBinding.buildingAddress.setText(building.address)
        }
        if (building.phone !== ""){
            fragBinding.editTextPhone.setText(building.phone)
        }

        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onPause() {
        building.name = fragBinding.buildingName.text.toString()
        building.address = fragBinding.buildingAddress.text.toString()
        building.phone = fragBinding.editTextPhone.text.toString()
        building.image = uri.toString()
        super.onPause()
    }

    override fun onResume() {
        setButtonListener(fragBinding)
        super.onResume()
    }

    private fun setButtonListener(layout: FragmentBuildingBinding) {
        layout.btnAdd.setOnClickListener {

            building.id = Random().nextLong()
            building.name = layout.buildingName.text.toString()
            building.address = layout.buildingAddress.text.toString()
            building.phone = layout.editTextPhone.text.toString()
            if(building.image == "null") {
                building.image = uri.toString()
            }
            if (building.name.isEmpty()) {
                toast(R.string.loc_name)
            } else if (building.address.isEmpty()) {
                toast(R.string.loc_address)
            }  else if (building.phone.isEmpty()) {
                toast(R.string.loc_phone)
            }  else if ( building.image == "null") {
                toast(R.string.loc_img)
            }else {
                app.builds.create(building)
                Timber.i(building.toString())
                uri = null
                building.name = ""
                layout.buildingName.setText("")
                layout.buildingAddress.setText("")
                layout.editTextPhone.setText("")
                layout.buildingImage.setImageURI(null)
                building.phone = ""
                building.address = ""
                building.image = "null"
                it.findNavController()
                    .navigate(R.id.action_buildingFragment_to_buildingListFragment)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,
            requireView().findNavController()) || super.onOptionsItemSelected(item)
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            BuildingFragment().apply {
                arguments = Bundle().apply {}
            }
    }

}