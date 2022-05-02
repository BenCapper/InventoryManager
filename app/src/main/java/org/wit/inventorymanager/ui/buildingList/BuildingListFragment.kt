package org.wit.inventorymanager.ui.buildingList


import android.annotation.SuppressLint
import android.app.AlertDialog
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
import org.wit.inventorymanager.helpers.*
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
    lateinit var loader : AlertDialog
    private lateinit var swipeCallback: TouchHelpers
    private lateinit var foundList: ArrayList<BuildingModel>
    private val buildingListViewModel: BuildingListViewModel by activityViewModels()
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
        loader = createLoader(requireActivity())
        activity?.title = getString(R.string.action_location)
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)

        showLoader(loader, "Downloading Building")
        buildingListViewModel.observableBuildingList.observe(viewLifecycleOwner, Observer { building ->
            building?.let {
                render(building as ArrayList<BuildingModel>)
                hideLoader(loader)
                checkSwipeRefresh()
            }
        })
        setSwipeRefresh()

        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showLoader(loader, "Deleting Building")
                val adapter = fragBinding.recyclerView.adapter as BuildingAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                buildingListViewModel.delete(
                    buildingListViewModel.liveFirebaseUser.value?.uid!!,
                    (viewHolder.itemView.tag as BuildingModel).uid!!
                )
                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)

        val swipeEditHandler = object : SwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onBuildingClick(viewHolder.itemView.tag as BuildingModel)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)
        removeBuildingData()
        getSearchData()

        return root
    }

    private fun render(buildingList: ArrayList<BuildingModel>) {
        fragBinding.recyclerView.adapter = BuildingAdapter(buildingList,this, buildingListViewModel.readOnly.value!!)
    }

    private fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {
            fragBinding.swiperefresh.isRefreshing = true
            showLoader(loader, "Downloading Buildings")
            if(buildingListViewModel.readOnly.value!!)
                buildingListViewModel.loadAll()
            else
                buildingListViewModel.load()
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader, "Downloading Buildings")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                buildingListViewModel.liveFirebaseUser.value = firebaseUser
                buildingListViewModel.load()
            }
        })
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
    private fun showBuildings (buildingList: ArrayList<BuildingModel>) {
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.adapter = BuildingAdapter(buildingList, this@BuildingListFragment, buildingListViewModel.readOnly.value!!)
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.adapter?.notifyDataSetChanged()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}