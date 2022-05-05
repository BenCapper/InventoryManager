package org.wit.inventorymanager.ui.buildingDetail

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingDetailBinding
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.building.BuildingViewModel
import org.wit.inventorymanager.ui.buildingList.BuildingListViewModel
import org.wit.inventorymanager.ui.maps.MapsViewModel
import splitties.snackbar.snack
import timber.log.Timber

class BuildingDetailFragment : Fragment() {

    private var nFragBinding: FragmentBuildingDetailBinding? = null
    private val fragBinding get() = nFragBinding!!
    private var building = BuildingModel()
    private val args by navArgs<BuildingDetailFragmentArgs>()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private val buildingViewModel: BuildingViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val buildingListViewModel: BuildingListViewModel by activityViewModels()
    private val mapsViewModel: MapsViewModel by activityViewModels()
    private lateinit var buildingDetailViewModel: BuildingDetailViewModel
    var hire:Boolean?  = null

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

        buildingDetailViewModel = ViewModelProvider(this)[BuildingDetailViewModel::class.java]
        buildingDetailViewModel.observableBuild.observe(viewLifecycleOwner) { renderBuild() }

        val counties = resources.getStringArray(R.array.counties)
        val countyAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, counties)
        fragBinding.county.setAdapter(countyAdapter)

        if (args.building.lat.toString() != "default"){
            fragBinding.lat.setText(args.building.lat.toString())
            fragBinding.lng.setText(args.building.lat.toString())
        }

        fragBinding.staffQuantity.minValue = 1
        fragBinding.staffQuantity.maxValue = 100

        // Number picker listener
        fragBinding.staffQuantity.setOnValueChangedListener { _, _, newVal ->
        }
        // Only ever want to return to the buildList fragment from the back button to avoid weird maps interactions
        //requireActivity().onBackPressedDispatcher.addCallback(this) {
        // requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_buildingFragment_to_buildingListFragment)
        //}

        fragBinding.hiring.setOnCheckedChangeListener { _, isChecked ->
            hire = isChecked
        }


        fragBinding.buildingLocation.setOnClickListener {
            // val action = BuildingFragmentDirections.actionBuildingFragmentToMapsFragment()
            //requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
        }

        return root
    }


    override fun onPause() {
        building.name = fragBinding.buildingName.text.toString()
        building.phone = fragBinding.editTextPhone.text.toString()
        super.onPause()
    }

    private fun renderBuild() {
        fragBinding.buildDetailVm = buildingDetailViewModel
        Timber.i("Retrofit fragBinding.buildDetailVm == $fragBinding.buildDetailVm")
    }

    override fun onResume() {
        setButtonListener(fragBinding)
        super.onResume()
        val counties = resources.getStringArray(R.array.counties)
        val countyAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, counties)
        fragBinding.county.setAdapter(countyAdapter)
        buildingDetailViewModel.getBuild(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.building.id)
    }

    private fun setButtonListener(layout: FragmentBuildingDetailBinding) {
        layout.btnAdd.setOnClickListener {


            building.name = layout.buildingName.text.toString()
            building.phone = layout.editTextPhone.text.toString()

            // Input validation
            when {
                building.name.isEmpty() -> {
                    view?.snack(R.string.loc_name)
                }
                building.name.length > 15 -> {
                    view?.snack(R.string.b_name_chars)
                }
                building.phone.isEmpty() -> {
                    view?.snack(R.string.loc_phone)
                }
                building.phone.length > 15 -> {
                    view?.snack(R.string.b_phone_chars)
                }
                else -> {
                    building.id = args.building.id

                    building.hiring = hire
                    buildingDetailViewModel.updateBuild(loggedInViewModel.liveFirebaseUser.value?.uid!!,args.building.id,building)
                    view?.snack(R.string.b_create)
                    Timber.i(building.toString())
                    val action = BuildingDetailFragmentDirections.actionBuildingDetailFragmentToBuildingListFragment()

                    it.findNavController().navigate(action)
                }
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
            BuildingDetailFragment().apply {
                arguments = Bundle().apply {}
            }
    }


}