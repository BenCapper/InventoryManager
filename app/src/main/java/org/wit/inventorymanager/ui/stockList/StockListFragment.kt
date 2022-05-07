package org.wit.inventorymanager.ui.stockList


import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.wit.inventorymanager.adapters.StockAdapter
import org.wit.inventorymanager.adapters.StockListener
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentStockListBinding
import org.wit.inventorymanager.helpers.*
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.StockModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.building.BuildingViewModel
import org.wit.inventorymanager.ui.stockDetail.StockDetailFragmentArgs
import org.wit.inventorymanager.ui.stockDetail.StockDetailViewModel
import splitties.fragmentargs.arg


class StockListFragment : Fragment(), StockListener {

    private var _fragBinding: FragmentStockListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var buildings: MutableList<BuildingModel>
    lateinit var loader : AlertDialog
    private lateinit var foundList: ArrayList<StockModel>
    private val args by navArgs<StockListFragmentArgs>()
    private val stockListViewModel: StockListViewModel by activityViewModels()
    private val stockDetailViewModel: StockDetailViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val buildingViewModel : BuildingViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _fragBinding = FragmentStockListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        loader = createLoader(requireActivity())
        activity?.title = getString(R.string.action_location)
        fragBinding.srecyclerView.layoutManager = LinearLayoutManager(activity)
        showLoader(loader, "Downloading Stock")
        stockListViewModel.observableStockList.observe(viewLifecycleOwner, Observer { stock ->
            stock?.let {
                render(stock as ArrayList<StockModel>)
                hideLoader(loader)
                checkSwipeRefresh()
            }
        })
        setSwipeRefresh()

        fragBinding.sfab.setOnClickListener {
            val action = StockListFragmentDirections.actionStockListFragmentToStockFragment(args.buildingid)
            findNavController().navigate(action)
        }
        fragBinding.stockSearch.setOnQueryTextListener(object :  SearchView.OnQueryTextListener  {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    stockListViewModel.search(loggedInViewModel.liveFirebaseUser.value?.uid!!,args.buildingid, newText)
                }
                else {

                }
                return true
            }
        })



        val swipeDeleteHandler = object : StockSwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showLoader(loader, "Deleting Stock")
                val adapter = fragBinding.srecyclerView.adapter as StockAdapter
                adapter.removeAt(viewHolder.absoluteAdapterPosition)
                stockListViewModel.delete(
                    stockListViewModel.liveFirebaseUser.value?.uid!!,
                    (viewHolder.itemView.tag as StockModel).id!!
                )
                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.srecyclerView)

        val swipeEditHandler = object : StockSwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onEditSwipe(viewHolder.itemView.tag as StockModel)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.srecyclerView)


        return root
    }



    private fun render(stockList: ArrayList<StockModel>) {
        fragBinding.srecyclerView.adapter = StockAdapter(stockList,this, stockListViewModel.readOnly.value!!)

    }

    override fun onFave(stock: StockModel) {
        if (stock.faved){
            stock.faved = false
            stockListViewModel.update(stock.uid, stock.id, stock)
        }
        else if (!stock.faved){
            stock.faved = true
            stockListViewModel.update(stock.uid, stock.id, stock)
        }

    }

    override fun onStockClick(stock: StockModel) {

    }

    private fun setSwipeRefresh() {
        fragBinding.sswiperefresh.setOnRefreshListener {
            fragBinding.sswiperefresh.isRefreshing = true
            showLoader(loader, "Downloading stock")
            if(stockListViewModel.readOnly.value!!)
                stockListViewModel.loadAll(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.buildingid)
            else
                stockListViewModel.load()
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.sswiperefresh.isRefreshing)
            fragBinding.sswiperefresh.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader, "Downloading Stock")

        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                stockListViewModel.liveFirebaseUser.value = firebaseUser
                stockListViewModel.loadAll(firebaseUser.uid, args.buildingid)
            }
        })
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_building, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val action = StockListFragmentDirections.actionStockListFragmentToStockFragment(args.buildingid)
        if (id == R.id.item_new){
            findNavController().navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }



    override fun onEditSwipe(stock: StockModel) {
        val action = StockListFragmentDirections.actionStockListFragmentToStockDetailFragment(stock, args.buildingid)
        if(!stockListViewModel.readOnly.value!!)
            findNavController().navigate(action)
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }


    override fun onAddStockClick(stock: StockModel) {

    }

    override fun onMinusStockClick(stock: StockModel) {

    }

}
