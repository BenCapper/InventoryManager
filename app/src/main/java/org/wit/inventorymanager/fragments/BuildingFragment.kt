package org.wit.inventorymanager.fragments


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.squareup.picasso.Picasso
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingBinding
import org.wit.inventorymanager.helpers.showImagePicker
import org.wit.inventorymanager.main.InventoryApp
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.Location
import splitties.toast.toast
import timber.log.Timber
import java.util.*


lateinit var app: InventoryApp


private var _fragBinding: FragmentBuildingBinding? = null
private val fragBinding get() = _fragBinding!!
private var building = BuildingModel()
private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>

class BuildingFragment : Fragment() {


    private var id: Long = 0

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
        registerImagePickerCallback()
        val bundle = arguments
        if (arguments?.isEmpty == false) {
            building = bundle?.getParcelable("editBuild")!!


            if (building.id.toString() !== "0" && building.id.toString() !== null ){
                id = building.id
                fragBinding.btnAdd.setText(R.string.up_loc)
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
            if (building.image !== ""){
                Picasso.get()
                    .load(Uri.parse(building.image))
                    .into(fragBinding.buildingImage)
                fragBinding.chooseImage.setText(R.string.img_ch)
            }
        }

        if(building.image !== ""){
            Picasso.get()
                .load(Uri.parse(building.image))
                .into(fragBinding.buildingImage)
            fragBinding.chooseImage.setText(R.string.img_ch)

        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
        requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_buildingFragment_to_buildingListFragment)
        }

        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }

        fragBinding.buildingLocation.setOnClickListener {
            val location = Location(52.245696, -7.139102, 15f)
            val action = BuildingFragmentDirections.actionBuildingFragmentToMapsFragment()
            if (building.zoom == 15f && building.lat == 52.245696 && building.lng == -7.139102 && building.id.toString().length > 1) {
                action.arguments.putParcelable("build", building)
                requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
            }
            if (building.zoom != 0f && building.lat != 0.toDouble() && building.lng != 0.toDouble()) {
                action.arguments.putParcelable("build", building)
                requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
            }
            else {
                building.zoom = location.zoom
                building.lat = location.lat
                building.lng = location.lng
                action.arguments.putParcelable("build", building)
                requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
            }
        }
        return root
    }

    override fun onPause() {
        building.name = fragBinding.buildingName.text.toString()
        building.address = fragBinding.buildingAddress.text.toString()
        building.phone = fragBinding.editTextPhone.text.toString()
        super.onPause()
    }

    override fun onResume() {
        setButtonListener(fragBinding)
        super.onResume()
    }

    private fun setButtonListener(layout: FragmentBuildingBinding) {
        layout.btnAdd.setOnClickListener {

            if (building.id.toString().length == 1) {
                building.id = Random().nextLong()
            }
            building.name = layout.buildingName.text.toString()
            building.address = layout.buildingAddress.text.toString()
            building.phone = layout.editTextPhone.text.toString()

            if (building.name.isEmpty()) {
                toast(R.string.loc_name)
            } else if (building.address.isEmpty()) {
                toast(R.string.loc_address)
            }  else if (building.phone.isEmpty()) {
                toast(R.string.loc_phone)
            }  else if ( building.image == "null") {
                toast(R.string.loc_img)
            }else {
                if (id.toString().length > 1){
                    app.builds.update(building)
                }
                else {
                    app.builds.create(building)
                }
                Timber.i(building.toString())

                layout.buildingName.setText("")
                layout.buildingAddress.setText("")
                layout.editTextPhone.setText("")
                layout.buildingImage.setImageURI(null)
                building.name = ""
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

    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${result.data!!.data}")
                            building.image = result.data!!.data!!.toString()
                            Picasso.get()
                                .load(building.image)
                                .into(fragBinding.buildingImage)
                            fragBinding.chooseImage.setText(R.string.change_building_image)
                        }
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            BuildingFragment().apply {
                arguments = Bundle().apply {}
            }
    }

}