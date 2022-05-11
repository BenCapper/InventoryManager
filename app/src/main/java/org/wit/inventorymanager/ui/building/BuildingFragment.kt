package org.wit.inventorymanager.ui.building


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingBinding
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import splitties.snackbar.snack
import kotlin.random.Random


class BuildingFragment : Fragment() {

    private var nFragBinding: FragmentBuildingBinding? = null
    private val fragBinding get() = nFragBinding!!
    private lateinit var buildingViewModel: BuildingViewModel
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private var hire: Boolean? = null
    var staff = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        nFragBinding = FragmentBuildingBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        activity?.title = getString(R.string.action_location)

        buildingViewModel = ViewModelProvider(this)[BuildingViewModel::class.java]
        buildingViewModel.observableStatus.observe(viewLifecycleOwner) { status ->
            status?.let { render(status) }
        }
        /* This is populating the dropdown menu with the counties from the counties array in the
        strings.xml file. */
        val counties = resources.getStringArray(R.array.counties)
        val countyAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, counties)
        fragBinding.county.setAdapter(countyAdapter)

        /* This is a listener for the hiring switch. It sets the hire variable to true or false
        depending on the state of the switch. It also changes the colour of the switch text
        depending on the state of the switch. */
        fragBinding.hiring.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            hire = b
            if (fragBinding.hiring.isChecked){
                fragBinding.hiring.setTextColor(Color.argb(255,235, 172, 12))
            }
            else {
                fragBinding.hiring.setTextColor(Color.BLACK)
            }
        }
        fragBinding.staffQuantity.minValue = 0
        fragBinding.staffQuantity.maxValue = 100

        // Number picker listener
        fragBinding.staffQuantity.setOnValueChangedListener { _, _, newVal ->
            staff = newVal

        }
        // Only ever want to return to the buildList fragment from the back button to avoid weird maps interactions
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_buildingFragment_to_buildingListFragment)
        }

        setButtonListener(fragBinding)

        return root
    }

    /**
     * This function is called when the user clicks the "Set Location" button. It checks if the user
     * has entered all the required information and if so, it creates a new BuildingModel object and
     * passes it to the MapsFragment
     *
     * @param layout FragmentBuildingBinding - This is the binding object for the fragment.
     */
    private fun setButtonListener(layout: FragmentBuildingBinding){
        layout.buildingLocation.setOnClickListener {
            val newid = Random.nextLong().toString()
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
                        id = newid,
                        uid = loggedInViewModel.liveFirebaseUser.value?.uid!!,
                        name = fragBinding.buildingName.text.toString(),
                        town = fragBinding.town.text.toString(),
                        county = fragBinding.county.text.toString(),
                        staff = staff,
                        phone = fragBinding.editTextPhone.text.toString(),
                        hiring = hire,
                        faved = false
                    )
                    val action =
                        BuildingFragmentDirections.actionBuildingFragmentToMapsFragment(build)
                    findNavController().navigate(action)
                }
            }
        }
    }

    /**
     * > If the status is true, do something with the view, otherwise show a snackbar with the message
     * "Failed"
     *
     * @param status Boolean - This is the status of the operation.
     */
    private fun render(status: Boolean) {
        when (status) {
            true -> {
                view?.let {
                }
            }
            false -> view?.snack("Failed")
        }
    }


    override fun onResume() {
        /* This is setting the button listener for the "Set Location" button. It is also populating the
        dropdown menu with the counties from the counties array in the strings.xml file. */
        setButtonListener(fragBinding)
        val counties = resources.getStringArray(R.array.counties)
        val countyAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, counties)
        fragBinding.county.setAdapter(countyAdapter)
        super.onResume()
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,
            requireView().findNavController()) || super.onOptionsItemSelected(item)
    }


}