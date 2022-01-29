package org.wit.inventorymanager.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingBinding
import org.wit.inventorymanager.main.InventoryApp
import org.wit.inventorymanager.models.BuildingModel

lateinit var app: InventoryApp
private var _fragBinding: FragmentBuildingBinding? = null
private val fragBinding get() = _fragBinding!!
private var building = BuildingModel()


class BuildingFragment : Fragment() {

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
        activity?.title = getString(R.string.action_location)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onResume() {
        super.onResume()
    }

    private fun setButtonListener(layout: FragmentBuildingBinding) {
        layout.btnAdd.setOnClickListener {
            building.name = layout.buildingName.text.toString()
            building.address = layout.buildingAddress.text.toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_building, menu)
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