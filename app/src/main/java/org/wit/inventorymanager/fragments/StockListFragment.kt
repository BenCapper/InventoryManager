package org.wit.inventorymanager.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import androidx.activity.addCallback
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
            id = build.id
            Timber.i("Build = $id")
            view?.snack(id.toString())
        }
        if (arguments?.containsKey("stock") == true){
            stock = bundle?.getParcelable("stock")!!
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_stockListFragment_to_buildingListFragment)
        }
        getStockData()
        loadBranchStock()
        removeStockData()
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
        if (id == R.id.item_building_new) {
            bundle.putParcelable("build", build)
            bundle.putParcelable("stock", stock)
            findNavController().navigate(R.id.action_stockListFragment_to_stockFragment, bundle)
        }
        else {
            bundle.putParcelable("stock", stock)
            view?.findNavController()
                ?.navigate(R.id.action_stockListFragment_to_buildingListFragment, bundle)
        }
        return true
    }

    override fun onStockClick(stock: StockModel) {
        val action = StockListFragmentDirections.actionStockListFragmentToStockFragment()
        action.arguments.putParcelable("stock", stock)
        findNavController().navigate(action)
    }

    override fun onAddStockClick(stock: StockModel) {
        stock.inStock++
        app.stocks.update(stock)
    }

    override fun onMinusStockClick(stock: StockModel) {
        stock.inStock--
        app.stocks.update(stock)
    }



    private fun getStockData(){
        db.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                stockList = mutableListOf()
                if (snapshot.exists()) {
                    for (stockSnap in snapshot.children) {
                        val stock = stockSnap.getValue(StockModel::class.java)
                        if(stock?.branch == build.id){
                            stockList.add(stock)
                        }
                    }
                }
                showStock(stockList)
                if (stockList.isEmpty()) {
                    view?.findViewById<Button>(R.id.stockNoList)?.visibility = View.VISIBLE
                    view?.findViewById<Button>(R.id.stockNoList)?.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putParcelable("build", build)
                        it.findNavController()
                            .navigate(R.id.action_stockListFragment_to_stockFragment, bundle)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Timber.i("Failed: ${error.message}")
            }
        })
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

    private fun removeStockData() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (stockSnap in snapshot.children) {
                        val stock = stockSnap.getValue(StockModel::class.java)
                        stocks.add(stock!!)
                    }
                }
                swipeCallback = object : TouchHelpers() {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val pos = viewHolder.absoluteAdapterPosition
                        if (stocks.isNotEmpty()) {
                            app.stocks.delete(stocks[pos])
                            stocks.remove(stocks[pos])
                            fragBinding.sRecyclerView.adapter?.notifyItemRemoved(pos)
                        }
                    }
                }
                val itemTouchHelper = ItemTouchHelper(swipeCallback)
                itemTouchHelper.attachToRecyclerView(view?.findViewById(R.id.sRecyclerView))

            }

            override fun onCancelled(error: DatabaseError) {
                Timber.i("Failed: ${error.message}")
            }
        })
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
