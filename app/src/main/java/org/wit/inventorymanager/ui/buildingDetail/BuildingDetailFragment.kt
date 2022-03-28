package org.wit.inventorymanager.ui.buildingDetail

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.squareup.picasso.Picasso
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingDetailBinding
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.Location
import splitties.snackbar.snack
import timber.log.Timber
import kotlin.random.Random

class BuildingDetailFragment : Fragment() {

    private val args by navArgs<BuildingDetailFragmentArgs>()
    private var nFragBinding: FragmentBuildingDetailBinding? = null
    private val fragBinding get() = nFragBinding!!
    private var building = BuildingModel()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var buildingDetailViewModel: BuildingDetailViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        nFragBinding = FragmentBuildingDetailBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setButtonListener(fragBinding)
        activity?.title = getString(R.string.action_location)
        registerImagePickerCallback()
        buildingDetailViewModel = ViewModelProvider(this)[BuildingDetailViewModel::class.java]
        buildingDetailViewModel.observableBuild.observe(viewLifecycleOwner) { renderBuild() }

        buildingDetailViewModel.getBuild(args.buildingId.toString())
        try {
            building = buildingDetailViewModel.observableBuild?.value!!
            Picasso.get()
                .load(buildingDetailViewModel.observableBuild.value?.image)
                .into(fragBinding.buildingDetailImage)
        }
        catch (e:Exception){
            Timber.i("OBSERVABLE BUILD IS EMPTY!! $e")
        }

        // Only ever want to return to the buildList fragment from the back button to avoid weird maps interactions
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_buildingFragment_to_buildingListFragment)
        }

        fragBinding.chooseDetailImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }

        fragBinding.buildingDetailLocation.setOnClickListener {
            val action = BuildingDetailFragmentDirections.actionBuildingDetailFragmentToEditMapsFragment(
                args.buildingId.toString()
            )
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
        }
        return root
    }

    private fun renderBuild() {
        fragBinding.buildDetailVm = buildingDetailViewModel
        Timber.i("Retrofit fragBinding.buildDetailVm == $fragBinding.buildDetailVm")
    }


    override fun onResume() {
        super.onResume()
        setButtonListener(fragBinding)
        buildingDetailViewModel.getBuild(args.buildingId.toString())
    }

    private fun setButtonListener(layout: FragmentBuildingDetailBinding) {
        layout.btnDetailAdd.setOnClickListener {


            building.name = layout.buildingDetailName.text.toString()
            building.address = layout.buildingDetailAddress.text.toString()
            building.phone = layout.editTextDetailPhone.text.toString()

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
                building.zoom == 0F && building.lat == (0).toDouble() && building.lng == (0).toDouble() -> {
                    view?.snack(R.string.loc_none)
                }
                else -> {
                    buildingDetailViewModel.updateBuild(args.buildingId.toString(), building)
                    Timber.i(building.toString())
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
                                .into(fragBinding.buildingDetailImage)
                            fragBinding.chooseDetailImage.setText(R.string.change_building_image)
                        }
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
    }

    private fun showImagePicker(intentLauncher: ActivityResultLauncher<Intent>) {
        var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
        chooseFile.type = "image/*"
        chooseFile = Intent.createChooser(chooseFile, R.string.button_addImage.toString())
        intentLauncher.launch(chooseFile)
    }


}