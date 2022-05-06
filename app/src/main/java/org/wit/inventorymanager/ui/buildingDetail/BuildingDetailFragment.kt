package org.wit.inventorymanager.ui.buildingDetail

import android.content.Intent
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingDetailBinding
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.building.BuildingFragmentDirections
import org.wit.inventorymanager.ui.building.BuildingViewModel
import org.wit.inventorymanager.ui.buildingList.BuildingListViewModel
import org.wit.inventorymanager.ui.maps.MapsViewModel
import splitties.snackbar.snack
import timber.log.Timber
import kotlin.random.Random

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
    var staff = 0

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
            staff = newVal
        }
        // Only ever want to return to the buildList fragment from the back button to avoid weird maps interactions
        requireActivity().onBackPressedDispatcher.addCallback(this) {
         findNavController().navigate(R.id.action_buildingDetailFragment_to_buildingListFragment)
        }

        fragBinding.hiring.setOnCheckedChangeListener { _, isChecked ->
            hire = isChecked
            if (fragBinding.hiring.isChecked){
                fragBinding.hiring.setTextColor(Color.argb(255,235, 172, 12))
            }
            else {
                fragBinding.hiring.setTextColor(Color.BLACK)
            }
        }


        fragBinding.buildingLocation.setOnClickListener {
            val action = BuildingDetailFragmentDirections.actionBuildingDetailFragmentToEditMapsFragment(args.building)
            findNavController().navigate(action)
        }

        return root
    }


    private fun renderBuild() {
        fragBinding.buildDetailVm = buildingDetailViewModel
        Timber.i("Retrofit fragBinding.buildDetailVm == $fragBinding.buildDetailVm")
    }

    override fun onResume() {
        setButtonListener(fragBinding)
        buildingDetailViewModel.getBuild(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.building.id)
        val counties = resources.getStringArray(R.array.counties)
        val countyAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, counties)
        fragBinding.county.setAdapter(countyAdapter)
        fragBinding.staffQuantity.value = args.building.staff
        super.onResume()

    }

    private fun setButtonListener(layout: FragmentBuildingDetailBinding) {
        layout.btnAdd.setOnClickListener {
            when {
                fragBinding.buildingName.text.toString().isEmpty() -> {
                    view?.snack(R.string.warn_name)
                }
                fragBinding.buildingName.text.toString().length > 15 -> {
                    view?.snack(R.string.warn_name_len)
                }
                fragBinding.editTextPhone.text.toString().isEmpty() -> {
                    view?.snack(R.string.warn_phone)
                }
                fragBinding.editTextPhone.text.toString().length > 10 -> {
                    view?.snack(R.string.warn_phone_len)
                }
                fragBinding.town.text.toString().isEmpty() -> {
                    view?.snack(R.string.warn_town)
                }
                fragBinding.county.text.toString() == "Select County" || fragBinding.county.text.toString().isEmpty() -> {
                    view?.snack(R.string.warn_county)
                }
                fragBinding.staffQuantity.value == 0 -> {
                    view?.snack(R.string.warn_staff)
                }
                else -> {
                    if(hire == null){
                        hire = false
                    }
                    var build = BuildingModel(
                        id = args.building.id,
                        uid = loggedInViewModel.liveFirebaseUser.value?.uid!!,
                        name = layout.buildingName.text.toString(),
                        town = layout.town.text.toString(),
                        county =  layout.county.text.toString(),
                        staff = layout.staffQuantity.value,
                        phone = layout.editTextPhone.text.toString(),
                        hiring = hire,
                        lat = layout.lat.text.toString(),
                        lng = layout.lng.text.toString()
                    )
                    buildingDetailViewModel.updateBuild(args.building.uid,args.building.id,build)
                    val action = BuildingDetailFragmentDirections.actionBuildingDetailFragmentToBuildingListFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val action = BuildingDetailFragmentDirections.actionBuildingDetailFragmentToBuildingListFragment()
        requireView().findNavController().navigate(action)
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            BuildingDetailFragment().apply {
                arguments = Bundle().apply {}
            }
    }


}