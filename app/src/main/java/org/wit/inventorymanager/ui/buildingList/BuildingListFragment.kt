package org.wit.inventorymanager.ui.buildingList


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import org.wit.inventorymanager.models.BuildingManager
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import timber.log.Timber

class BuildingListFragment : Fragment(), BuildingListener {


    private var _fragBinding: FragmentBuildingListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var buildings: MutableList<BuildingModel>
    private val db = FirebaseDatabase.getInstance("https://invmanage-4bcbd-default-rtdb.firebaseio.com")
        .getReference("Building")
    var builds = mutableListOf<BuildingModel>()
    private var build = BuildingModel()
    private var foundBuild = BuildingModel()
    private lateinit var swipeCallback: TouchHelpers
    private lateinit var foundList: MutableList<BuildingModel>
    private lateinit var buildingListViewModel: BuildingListViewModel
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        buildingListViewModel = ViewModelProvider(this).get(BuildingListViewModel::class.java)
        buildingListViewModel.observableBuildingList.observe(viewLifecycleOwner) { building_list ->
            building_list?.let { render(building_list) }
        }
        buildings = buildingListViewModel.observableBuildingList.value as MutableList<BuildingModel>
        removeBuildingData()
        getSearchData()

        return root
    }

    private fun render(buildingList: List<BuildingModel>) {
        fragBinding.recyclerView.adapter = BuildingAdapter(buildingList,this)
    }

    private fun getSearchData(){
        // https://stackoverflow.com/questions/55949305/how-to-properly-retrieve-data-from-searchview-in-kotlin
        fragBinding.buildingSearch.setOnQueryTextListener(object :  SearchView.OnQueryTextListener  {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    search(newText)
                }
                else {
                    showBuildings(foundList)
                }
                return true
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_building, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.item_building_new){
            findNavController().navigate(R.id.action_buildingListFragment_to_buildingFragment)
        }
        return super.onOptionsItemSelected(item)
}

    override fun onBuildingClick(building: BuildingModel) {
        // Open this buildings stock list
        val bundle = Bundle()
        bundle.putParcelable("id", building)
        findNavController().navigate(R.id.action_buildingListFragment_to_stockListFragment, bundle)

    }

    override fun onEditBuildingClick(building: BuildingModel) {
        // Send building info to the create building fragment
        val action = BuildingListFragmentDirections.actionBuildingListFragmentToBuildingDetailFragment(building.id)
        findNavController().navigate(action)
    }


    private fun search(newText: String){
        foundList = mutableListOf()
        for(item in buildings){
            if(item.name.lowercase().contains(newText.lowercase())){
                foundList.add(item)
            }
        }
        showBuildings(foundList)
    }

    private fun removeBuildingData(){
        /* Get list of buildings saved to the database
         * delete the one at the swiped position
         */
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
                            buildingListViewModel.delete(loggedInViewModel.liveFirebaseUser.value!!.uid, builds[pos].id)
                            builds.remove(builds[pos])
                            fragBinding.recyclerView.adapter?.notifyItemRemoved(pos)
                        }
                    }
                }
                val itemTouchHelper = ItemTouchHelper(swipeCallback)
                itemTouchHelper.attachToRecyclerView(view?.findViewById(R.id.recyclerView))

            }
            override fun onCancelled(error: DatabaseError) {
                Timber.i("Failed: ${error.message}")
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showBuildings (buildingList: List<BuildingModel>) {
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.adapter = BuildingAdapter(buildingList, this@BuildingListFragment)
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.adapter?.notifyDataSetChanged()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}