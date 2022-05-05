package org.wit.inventorymanager.ui.stockList


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import org.wit.inventorymanager.adapters.StockAdapter
import org.wit.inventorymanager.adapters.StockListener
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentStockListBinding

import org.wit.inventorymanager.helpers.TouchHelpers
import org.wit.inventorymanager.main.InventoryApp
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.StockModel
import splitties.snackbar.snack
import timber.log.Timber


class StockListFragment : Fragment(), StockListener {

    lateinit var app: InventoryApp
    private var _fragBinding: FragmentStockListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var stockList: MutableList<StockModel>
    private val db =
        FirebaseDatabase.getInstance("https://invmanage-4bcbd-default-rtdb.firebaseio.com")
            .getReference("Stock")
    var stocks = mutableListOf<StockModel>()
    private var stock = StockModel()
    private lateinit var swipeCallback: TouchHelpers
    private lateinit var foundList: MutableList<StockModel>
    private var id = (0).toLong()
    private var build = BuildingModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as InventoryApp
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragBinding = FragmentStockListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.sRecyclerView.layoutManager = LinearLayoutManager(activity)
        val bundle = arguments
        if (arguments?.containsKey("id") == true) {
            build = bundle?.getParcelable("id")!!
            Timber.i("Build = $id")
            view?.snack(id.toString())
        }
        if (arguments?.containsKey("stock") == true){
            stock = bundle?.getParcelable("stock")!!
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_stockListFragment_to_buildingListFragment)
        }
        loadBranchStock()
        getSearchStockData()




        return root
    }

    private fun getSearchStockData() {
        //https://stackoverflow.com/questions/55949305/how-to-properly-retrieve-data-from-searchview-in-kotlin
        fragBinding.stockSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    search(newText)
                } else {
                    showStock(foundList)
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
        val bundle = Bundle()
        val id = item.itemId
        if (id == R.id.item_confirm) {
            bundle.putParcelable("build", build)
            bundle.putParcelable("stock", stock)
            findNavController().navigate(R.id.action_stockListFragment_to_stockFragment, bundle)
        }
        else {
            // Nav drawer option selected, never want to return to the edit screen, go to building list
            bundle.putParcelable("stock", stock)
            view?.findNavController()
                ?.navigate(R.id.action_stockListFragment_to_buildingListFragment, bundle)
        }
        return true
    }

    override fun onStockClick(stock: StockModel) {
        // Send stock info to create stock fragment for editing
        val action = StockListFragmentDirections.actionStockListFragmentToStockFragment()
        action.arguments.putParcelable("stock", stock)
        findNavController().navigate(action)
    }

    override fun onAddStockClick(stock: StockModel) {
        // Add one to stock item quantity
        stock.inStock++
        app.stocks.update(stock)

    }

    override fun onMinusStockClick(stock: StockModel) {
        // Remove 1 from stock item quantity unless already 0
        if (stock.inStock >= 1) {
            stock.inStock--
            app.stocks.update(stock)
        } else {
            view?.snack(R.string.quantity_warn)
        }
    }



    private fun loadBranchStock(){
        val filteredStock = app.stocks.filterStock(id)
        showStock(filteredStock)
    }

    private fun search(newText: String) {
        foundList = mutableListOf()
        for (item in stockList) {
            if (item.name.lowercase().contains(newText.lowercase())) {
                foundList.add(item)
            }
        }
        showStock(foundList)
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun showStock(stockList: List<StockModel>) {
        view?.findViewById<RecyclerView>(R.id.sRecyclerView)?.adapter =
            StockAdapter(stockList, this@StockListFragment)
        view?.findViewById<RecyclerView>(R.id.sRecyclerView)?.adapter?.notifyDataSetChanged()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}