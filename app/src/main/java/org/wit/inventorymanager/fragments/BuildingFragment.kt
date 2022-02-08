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

    private var uri: Uri? =  null
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
            building.zoom = bundle?.getFloat("loc")!!
            building.lng = bundle?.getDouble("lng")!!
            building.lat = bundle?.getDouble("lat")!!
            if (arguments?.containsKey("editId") == true){
                id = arguments?.getLong("editId")!!
                fragBinding.btnAdd.setText(R.string.up_loc)
            }
            if (arguments?.containsKey("editName") == true){
                building.name = bundle?.getString("editName")!!
                fragBinding.buildingName.setText(bundle?.getString("editName"))
            }
            if (arguments?.containsKey("editAddress") == true){
                building.address = bundle?.getString("editAddress")!!
                fragBinding.buildingAddress.setText(bundle?.getString("editAddress"))
            }
            if (arguments?.containsKey("editPhone") == true){
                building.phone = bundle?.getString("editPhone")!!
                fragBinding.editTextPhone.setText(bundle?.getString("editPhone"))
            }
            if (arguments?.containsKey("editUri") == true){
                building.image = bundle?.getString("editUri")!!
                Picasso.get()
                    .load(Uri.parse(bundle?.getString("editUri")))
                    .into(fragBinding.buildingImage)
                fragBinding.chooseImage.setText(R.string.img_ch)
            }
        }

        if(building.image !== "null"){
            Picasso.get()
                .load(Uri.parse(building.image))
                .into(fragBinding.buildingImage)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
        requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_buildingFragment_to_buildingListFragment)
        }

        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }

        fragBinding.buildingLocation.setOnClickListener {
            val location = Location(52.245696, -7.139102, 15f)
            if (building.zoom != 0f) {
                location.lat = building.lat
                location.lng = building.lng
                location.zoom = building.zoom
            }

            val action = BuildingFragmentDirections.actionBuildingFragmentToMapsFragment()
            action.arguments.putFloat("loc", location.zoom)
            action.arguments.putDouble("lat", location.lat)
            action.arguments.putDouble("lng", location.lng)
            action.arguments.putString("editUri", building.image)
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
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

            building.id = Random().nextLong()
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
                if (arguments?.containsKey("editId") == true){
                    building.id = id
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
                        } // end of if
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