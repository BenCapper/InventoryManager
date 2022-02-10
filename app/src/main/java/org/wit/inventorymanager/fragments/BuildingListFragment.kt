package org.wit.inventorymanager.fragments


import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.wit.inventorymanager.R
import org.wit.inventorymanager.adapters.BuildingAdapter
import org.wit.inventorymanager.adapters.BuildingListener
import org.wit.inventorymanager.databinding.FragmentBuildingListBinding
import org.wit.inventorymanager.helpers.TouchHelpers
import org.wit.inventorymanager.main.InventoryApp
import org.wit.inventorymanager.models.BuildingModel
import timber.log.Timber

class BuildingListFragment : Fragment(), BuildingListener {

    lateinit var app: InventoryApp
    private var _fragBinding: FragmentBuildingListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var buildings: MutableList<BuildingModel>
    private val db = FirebaseDatabase.getInstance("https://invmanage-4bcbd-default-rtdb.firebaseio.com")
        .getReference("Building")
    var builds = mutableListOf<BuildingModel>()
    private var build = BuildingModel()
    private lateinit var swipeCallback: TouchHelpers


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
        removeBuildingData()




        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_building, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item!!.itemId
        if (id == R.id.item_building_new){
            findNavController().navigate(R.id.action_buildingListFragment_to_buildingFragment)
        }
        return super.onOptionsItemSelected(item)
}

    override fun onBuildingClick(building: BuildingModel) {
        TODO("Not yet implemented")
    }

    override fun onEditBuildingClick(building: BuildingModel) {
        val action = BuildingListFragmentDirections.actionBuildingListFragmentToBuildingFragment()
        build.name = building.name
        build.address = building.address
        build.phone = building.phone
        build.image = building.image
        build.id = building.id
        build.zoom = building.zoom
        build.lat = building.lat
        build.lng = building.lng
        action.arguments.putParcelable("editBuild", build)
        findNavController().navigate(action)
    }


    private fun getBuildingData(){
        db.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                buildings = mutableListOf()
                if(snapshot.exists()){
                    for(buildSnap in snapshot.children){
                        val build = buildSnap.getValue(BuildingModel::class.java)
                        buildings.add(build!!)
                    }
                }
                view?.findViewById<RecyclerView>(R.id.recyclerView)?.adapter = BuildingAdapter(buildings, this@BuildingListFragment)
                view?.findViewById<RecyclerView>(R.id.recyclerView)?.adapter?.notifyDataSetChanged()
                if (buildings.isEmpty()) {
                    view?.findViewById<Button>(R.id.noList)?.visibility = View.VISIBLE
                    view?.findViewById<Button>(R.id.noList)?.setOnClickListener {
                        it.findNavController()
                            .navigate(R.id.action_buildingListFragment_to_buildingFragment)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Failed", error.toException())
            }
        })
    }

    private fun removeBuildingData(){
        db.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(buildSnap in snapshot.children){
                        val build = buildSnap.getValue(BuildingModel::class.java)
                        builds.add(build!!)
                    }
                }
                swipeCallback = object: TouchHelpers(){
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val pos = viewHolder.absoluteAdapterPosition
                        if (builds.isNotEmpty()) {
                            app.builds.delete(builds[pos])
                            builds.remove(builds[pos])
                            fragBinding.recyclerView.adapter?.notifyItemRemoved(pos)
                        }
                    }
                }
                val itemTouchHelper = ItemTouchHelper(swipeCallback)
                itemTouchHelper.attachToRecyclerView(view?.findViewById(R.id.recyclerView))

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