package org.wit.inventorymanager.ui.building


import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.core.text.set
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import splitties.snackbar.snack
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingBinding
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.buildingList.BuildingListViewModel
import org.wit.inventorymanager.ui.maps.MapsViewModel
import timber.log.Timber


class BuildingFragment : Fragment() {

    private var nFragBinding: FragmentBuildingBinding? = null
    private val fragBinding get() = nFragBinding!!
    private var building = BuildingModel()
    private val args by navArgs<BuildingFragmentArgs>()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var buildingViewModel: BuildingViewModel
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val buildingListViewModel: BuildingListViewModel by activityViewModels()
    private val mapsViewModel: MapsViewModel by activityViewModels()


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

        buildingViewModel = ViewModelProvider(this)[BuildingViewModel::class.java]
        buildingViewModel.observableStatus.observe(viewLifecycleOwner) { status ->
            status?.let { render(status) }
        }
        val counties = resources.getStringArray(R.array.counties)
        val countyAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, counties)
        fragBinding.county.setAdapter(countyAdapter)

        if (args.lat != "default"){
            fragBinding.lat.setText(args.lat)
            fragBinding.lng.setText(args.lng)
        }

        fragBinding.staffQuantity.minValue = 1
        fragBinding.staffQuantity.maxValue = 100

        // Number picker listener
        fragBinding.staffQuantity.setOnValueChangedListener { _, _, newVal ->
        }
        // Only ever want to return to the buildList fragment from the back button to avoid weird maps interactions
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_buildingFragment_to_buildingListFragment)
        }



        fragBinding.buildingLocation.setOnClickListener {
            val action = BuildingFragmentDirections.actionBuildingFragmentToMapsFragment()
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
            }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().snack("${args.lat} ${args.lng}")
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

    override fun onPause() {
        building.name = fragBinding.buildingName.text.toString()
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
                else -> {
                    buildingViewModel.addBuilding(loggedInViewModel.liveFirebaseUser,building)
                    view?.snack(R.string.b_create)
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

    companion object {
        @JvmStatic
        fun newInstance() =
            BuildingFragment().apply {
                arguments = Bundle().apply {}
            }
    }


}