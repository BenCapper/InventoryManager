package org.wit.inventorymanager.ui.buildingDetail

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
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingDetailBinding
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import splitties.snackbar.snack
import timber.log.Timber


class BuildingDetailFragment : Fragment() {

    private var nFragBinding: FragmentBuildingDetailBinding? = null
    private val fragBinding get() = nFragBinding!!
    private val args by navArgs<BuildingDetailFragmentArgs>()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private lateinit var buildingDetailViewModel: BuildingDetailViewModel
    private var hire:Boolean?  = null
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

        /* This is setting up the dropdown menu for the county field. */
        val counties = resources.getStringArray(R.array.counties)
        val countyAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, counties)
        fragBinding.county.setAdapter(countyAdapter)

        if (args.building.lat != "default"){
            fragBinding.lat.setText(args.building.lat)
            fragBinding.lng.setText(args.building.lat)
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

        /* This is setting up the listener for the hiring checkbox. If the checkbox is checked, the
        text color is changed to orange. If it is not checked, the text color is changed to black. */
        fragBinding.hiring.setOnCheckedChangeListener { _, isChecked ->
            hire = isChecked
            if (fragBinding.hiring.isChecked){
                fragBinding.hiring.setTextColor(Color.argb(255,235, 172, 12))
            }
            else {
                fragBinding.hiring.setTextColor(Color.BLACK)
            }
        }


        /* This is setting up the listener for the location button. When the button is clicked, the
        user is navigated to the EditMapsFragment. */
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

    /**
     * This is the code that is executed when the user clicks the save button. It checks to make sure
     * that all the fields are filled in correctly. If they are not, it displays a snackbar with the
     * appropriate warning. If they are, it creates a new BuildingModel object and passes it to the
     * updateBuild function in the BuildingDetailViewModel
     *
     * @param layout FragmentBuildingDetailBinding - This is the binding object for the fragment.
     */
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
                    val build = BuildingModel(
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




}