package org.wit.inventorymanager.ui.buildingList


import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.wit.inventorymanager.R
import org.wit.inventorymanager.adapters.BuildingAdapter
import org.wit.inventorymanager.adapters.BuildingListener
import org.wit.inventorymanager.databinding.FragmentBuildingListBinding
import org.wit.inventorymanager.helpers.*
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.buildingDetail.BuildingDetailViewModel

/* This is the code for the building list fragment. It is the fragment that displays the list of
buildings. */
class BuildingListFragment : Fragment(), BuildingListener {

    private var _fragBinding: FragmentBuildingListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var buildings: MutableList<BuildingModel>
    lateinit var loader : AlertDialog
    private lateinit var foundList: ArrayList<BuildingModel>
    private val buildingListViewModel: BuildingListViewModel by activityViewModels()
    private val buildingDetailViewModel: BuildingDetailViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()


    /**
     * It sets the menu for the activity.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this
     * is the state.
     */
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

        showLoader(loader, "Downloading Buildings")


        /* This is the code that observes the observable building list. It is observing the list for
        changes. When the list changes, it renders the list to the recycler view. */
        buildingListViewModel.observableBuildingList.observe(viewLifecycleOwner, Observer { building ->
            building?.let {
                render(building as ArrayList<BuildingModel>)
                hideLoader(loader)
                checkSwipeRefresh()
            }
        })
        setSwipeRefresh()

        /* This is the code that is executed when the floating action button is clicked. It navigates
        to the building fragment. */
        fragBinding.fab.setOnClickListener {
            val action = BuildingListFragmentDirections.actionBuildingListFragmentToBuildingFragment()
            findNavController().navigate(action)
        }



        /* This is the code that is executed when a building is swiped to delete. It removes the
        building from the recycler view and deletes it from the database. */
        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showLoader(loader, "Deleting Building")
                val adapter = fragBinding.recyclerView.adapter as BuildingAdapter
                adapter.removeAt(viewHolder.absoluteAdapterPosition)
                buildingListViewModel.delete(
                    buildingListViewModel.liveFirebaseUser.value?.uid!!,
                    (viewHolder.itemView.tag as BuildingModel).id!!
                )
                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)


        /* This is the code that is executed when a building is swiped to edit. It navigates to the
        building detail fragment. */
        val swipeEditHandler = object : SwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onEditSwipe(viewHolder.itemView.tag as BuildingModel)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)


        return root
    }



        /**
         * It renders the building list to the recycler view.
         *
         * @param buildingList ArrayList<BuildingModel> - This is the list of buildings that will be
         * displayed in the recycler view.
         */
        private fun render(buildingList: ArrayList<BuildingModel>) {
        fragBinding.recyclerView.adapter = BuildingAdapter(buildingList,this, buildingListViewModel.readOnly.value!!)
    }

    /**
     * This function is called when the user clicks the favorite button on the building detail page. If
     * the building is already favorited, it will unfavorite it. If the building is not favorited, it
     * will favorite it
     *
     * @param building BuildingModel - the building that was clicked on
     */
    override fun onFave(building: BuildingModel) {
        if (building.faved){
            building.faved = false
            buildingListViewModel.update(building.uid, building.id, building)
        }
        else if (!building.faved){
            building.faved = true
            buildingListViewModel.update(building.uid, building.id, building)
        }
    }


    /**
     * It sets the swipe refresh listener.
     */
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

    /**
     * It checks if the swipe refresh is refreshing and if it is, it sets it to false.
     */
    private fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }


    override fun onResume() {
        super.onResume()
        showLoader(loader, "Downloading Buildings")

        /* Observing the live firebase user. If the user is not null, it sets the live firebase user
        value to the firebase user and loads the buildings. */
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                buildingListViewModel.liveFirebaseUser.value = firebaseUser
                buildingListViewModel.load()
            }
        })
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_building, menu)

        /* Finding the search bar in the menu and setting it to the search view. */
        val item = menu.findItem(R.id.app_bar_search)
        val searchView = item.actionView as SearchView


        /* This is the code that is executed when the search bar is used. It searches the database for
        the building that the user is searching for. */
        searchView.setOnQueryTextListener(object :  SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    buildingListViewModel.search(
                        loggedInViewModel.liveFirebaseUser.value?.uid!!,
                        newText
                    )
                } else {

                }
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /* This is the code that is executed when the user clicks the add building button. It navigates
        to the building fragment. */
        val id = item.itemId
        if (id == R.id.item_new){
            findNavController().navigate(R.id.action_buildingListFragment_to_buildingFragment)
        }
        return super.onOptionsItemSelected(item)
}

    /**
     * > When a building is clicked, navigate to the stock list fragment for that building
     *
     * @param building BuildingModel - The building that was clicked
     */
    override fun onBuildingClick(building: BuildingModel) {
        // Open this buildings stock list
        val action = BuildingListFragmentDirections.actionBuildingListFragmentToStockListFragment(building.id)
        findNavController().navigate(action)
    }

    /**
     * If the user is not in read-only mode, navigate to the building detail fragment.
     *
     * @param building BuildingModel - The building that was swiped
     */
    override fun onEditSwipe(building: BuildingModel) {
        /* This is the code that is executed when a building is swiped to edit. It navigates to the
                building detail fragment. */
        val action = BuildingListFragmentDirections.actionBuildingListFragmentToBuildingDetailFragment(building)
        if(!buildingListViewModel.readOnly.value!!)
            findNavController().navigate(action)
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}