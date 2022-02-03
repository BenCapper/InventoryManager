package org.wit.inventorymanager.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingBinding
import org.wit.inventorymanager.main.InventoryApp
import org.wit.inventorymanager.models.BuildingModel
import timber.log.Timber
import java.util.*

lateinit var app: InventoryApp


private var _fragBinding: FragmentBuildingBinding? = null
private val fragBinding get() = _fragBinding!!
private var building = BuildingModel()



class BuildingFragment : Fragment() {

    private lateinit var uri: Uri

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
        val selectPictureLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            fragBinding.buildingImage.setImageURI(it)
            uri = it
        }
        fragBinding.chooseImage.setOnClickListener{
            selectPictureLauncher.launch("image/*")
        }


        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onResume() {
        super.onResume()
        setButtonListener(fragBinding)

    }

    private fun setButtonListener(layout: FragmentBuildingBinding) {
        layout.btnAdd.setOnClickListener {
            building.id = Random().nextLong()
            building.name = layout.buildingName.text.toString()
            building.address = layout.buildingAddress.text.toString()
            building.image = uri.toString()
            app.builds.create(building)
            Timber.i(building.toString())
            it.findNavController().navigate(R.id.action_buildingFragment_to_buildingListFragment)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //inflater.inflate(R.menu.menu_building, menu)
        super.onCreateOptionsMenu(menu, inflater)
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