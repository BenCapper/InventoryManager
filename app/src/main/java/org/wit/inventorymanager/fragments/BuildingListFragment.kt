package org.wit.inventorymanager.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.wit.inventorymanager.R
import org.wit.inventorymanager.adapters.BuildingAdapter
import org.wit.inventorymanager.databinding.FragmentBuildingListBinding
import org.wit.inventorymanager.main.InventoryApp
import org.wit.inventorymanager.models.BuildingModel
import timber.log.Timber

class BuildingListFragment : Fragment() {

    lateinit var app: InventoryApp
    private var _fragBinding: FragmentBuildingListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var builds: MutableList<BuildingModel>
    private val db = FirebaseDatabase.getInstance("https://invmanage-4bcbd-default-rtdb.firebaseio.com")
        .getReference("Building")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as InventoryApp
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _fragBinding = FragmentBuildingListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        activity?.title = getString(R.string.action_location)
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        getBuildingData()
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_building, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,
            requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    private fun showBuildings (buildings: List<BuildingModel>) {
        fragBinding.recyclerView.adapter = BuildingAdapter(buildings)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onResume(){
        super.onResume()
        getBuildingData()
    }

    private fun getBuildingData(){
        db.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                builds = mutableListOf()
                if(snapshot.exists()){
                    for(buildSnap in snapshot.children){
                        val build = buildSnap.getValue(BuildingModel::class.java)
                        builds.add(build!!)
                        Timber.i(build.toString())
                    }
                }
                showBuildings(builds)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Failed", error.toException())
            }
        })
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            BuildingListFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}