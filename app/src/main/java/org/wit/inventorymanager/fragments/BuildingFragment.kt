package org.wit.inventorymanager.fragments


import android.os.Bundle
import android.text.Editable
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
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
            var location = Location()
            location = bundle?.getParcelable<Location>("loc")!!
            building.zoom = location.zoom
            building.lng = location.lng
            building.lat = location.lat
            toast(building.lat.toString())

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
            it.findNavController().navigate(action)
        }


        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onResume() {
        super.onResume()
        setButtonListener(fragBinding)

    }

    private fun setButtonListener(layout: FragmentBuildingBinding) {
        layout.btnAdd.setOnClickListener {

            building.id = Random().nextLong()
            building.name = layout.buildingName.text.toString()
            building.address = layout.buildingAddress.text.toString()
            building.phone = layout.editTextPhone.text.toString()
            building.image = uri.toString()

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