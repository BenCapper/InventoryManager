package org.wit.inventorymanager.ui.building


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Switch
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingBinding
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.buildingList.BuildingListViewModel
import org.wit.inventorymanager.ui.maps.MapsViewModel
import splitties.snackbar.snack
import timber.log.Timber
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
        val counties = resources.getStringArray(R.array.counties)
        val countyAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, counties)
        fragBinding.county.setAdapter(countyAdapter)

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

    private fun setButtonListener(layout: FragmentBuildingBinding){
        layout.buildingLocation.setOnClickListener {
            var newid = Random.nextLong().toString()
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

    companion object {
        @JvmStatic
        fun newInstance() =
            BuildingFragment().apply {
                arguments = Bundle().apply {}
            }
    }


}