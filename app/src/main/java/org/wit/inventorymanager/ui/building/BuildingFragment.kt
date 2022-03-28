package org.wit.inventorymanager.ui.building


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgument
import androidx.navigation.ui.NavigationUI
import splitties.snackbar.snack
import com.squareup.picasso.Picasso
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingBinding

import org.wit.inventorymanager.main.InventoryApp
import org.wit.inventorymanager.models.BuildingManager
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.Location
import org.wit.inventorymanager.ui.buildingDetail.BuildingDetailFragmentArgs
import timber.log.Timber
import java.util.*


class BuildingFragment : Fragment() {

    private val args by navArgs<BuildingFragmentArgs>()
    private var nFragBinding: FragmentBuildingBinding? = null
    private val fragBinding get() = nFragBinding!!
    private var building = BuildingModel()
    private var foundBuild = BuildingModel()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var buildingViewModel: BuildingViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        nFragBinding = FragmentBuildingBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setButtonListener(fragBinding)
        activity?.title = getString(R.string.action_location)
        registerImagePickerCallback()
        buildingViewModel = ViewModelProvider(this).get(BuildingViewModel::class.java)
        buildingViewModel.observableStatus.observe(viewLifecycleOwner) { status ->
            status?.let { render(status) }
        }

        // Only ever want to return to the buildList fragment from the back button to avoid weird maps interactions
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_buildingFragment_to_buildingListFragment)
        }

        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }

        fragBinding.buildingLocation.setOnClickListener {
            val location = Location(52.245696, -7.139102, 15f)
            val action = BuildingFragmentDirections.actionBuildingFragmentToMapsFragment()

            if (args.zoom == 0f && args.lat.toDouble() == (0).toDouble() && args.lng.toDouble() == (0).toDouble()) {
                // Fresh location, set default values
                building.zoom = location.zoom
                building.lat = location.lat
                building.lng = location.lng
                action.arguments.putParcelable("build", building)
                requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
            }
            else {
                if (arguments?.containsKey("editBuild") == true) {
                    // Already have building info to send to maps
                    action.arguments.putParcelable("build", building)
                    requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
                }else{
                    // Set default values
                    building.id = (0).toLong()
                    building.zoom = 15f
                    building.lat = 52.245696
                    building.lng = -7.139102
                    action.arguments.putParcelable("build", building)
                    requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
                }
            }
        }
        return root
    }

    private fun render(status: Boolean) {
        when (status) {
            true -> {
                view?.let {
                }
            }
            false -> view?.snack("Failed")
        }
    }

    private fun renderBuild() {
        fragBinding.buildingvm = buildingViewModel
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


            building.name = layout.buildingName.text.toString()
            building.address = layout.buildingAddress.text.toString()
            building.phone = layout.editTextPhone.text.toString()

            // Input validation
            when {
                building.name.isEmpty() -> {
                    view?.snack(R.string.loc_name)
                }
                building.name.length > 15 -> {
                    view?.snack(R.string.b_name_chars)
                }
                building.address.isEmpty() -> {
                    view?.snack(R.string.loc_address)
                }
                building.address.length > 25 -> {
                    view?.snack(R.string.b_address_chars)
                }
                building.phone.isEmpty() -> {
                    view?.snack(R.string.loc_phone)
                }
                building.phone.length > 15 -> {
                    view?.snack(R.string.b_phone_chars)
                }
                building.image == "" -> {
                    view?.snack(R.string.loc_img)
                }
                else -> {
                    buildingViewModel.addBuilding(building)
                    view?.snack(R.string.b_create)
                    Timber.i(building.toString())

                    // Reset fields and variable values
                    layout.buildingName.setText("")
                    layout.buildingAddress.setText("")
                    layout.editTextPhone.setText("")
                    layout.buildingImage.setImageURI(null)
                    building.name = ""
                    building.phone = ""
                    building.address = ""
                    building.image = ""
                    building.id = 0
                    building.zoom = 0f
                    building.lat = (0).toDouble()
                    building.lng = (0).toDouble()


                    it.findNavController()
                        .navigate(R.id.action_buildingFragment_to_buildingListFragment)
                }
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

    private fun showImagePicker(intentLauncher: ActivityResultLauncher<Intent>) {
        var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
        chooseFile.type = "image/*"
        chooseFile = Intent.createChooser(chooseFile, R.string.button_addImage.toString())
        intentLauncher.launch(chooseFile)
    }

}