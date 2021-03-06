package org.wit.inventorymanager.ui.stockDetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentStockDetailBinding
import org.wit.inventorymanager.models.StockModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import splitties.snackbar.snack
import timber.log.Timber


class StockDetailFragment : Fragment() {

    private var nFragBinding: FragmentStockDetailBinding? = null
    private val fragBinding get() = nFragBinding!!
    private lateinit var stockDetailViewModel: StockDetailViewModel
    private val args by navArgs<StockDetailFragmentArgs>()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private var fav: Boolean? = null
    private var max = 0
    var current = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        nFragBinding = FragmentStockDetailBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        activity?.title = getString(R.string.action_stock)

        stockDetailViewModel = ViewModelProvider(this)[StockDetailViewModel::class.java]
        stockDetailViewModel.observableStock.observe(viewLifecycleOwner) { renderStock() }

        val units = resources.getStringArray(R.array.units)
        val unitAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, units)
        fragBinding.unitDetail.setAdapter(unitAdapter)
        current = fragBinding.stockDetailQuantity2.value

        fragBinding.stockDetailQuantity.minValue = 1
        fragBinding.stockDetailQuantity.maxValue = 1000

        fragBinding.stockDetailQuantity2.minValue = 0
        fragBinding.stockDetailQuantity2.maxValue = 1000

        // Number picker listener
        fragBinding.stockDetailQuantity.setOnValueChangedListener { _, _, newVal ->
            max = newVal
        }

        fragBinding.stockDetailQuantity2.setOnValueChangedListener { _, _, newVal ->
            current = newVal
        }


        setButtonListener(fragBinding)

        return root
    }

    private fun renderStock() {
        fragBinding.stockDetailVm = stockDetailViewModel
        Timber.i("Retrofit fragBinding.stockDetailVm == $fragBinding.stockDetailVm")
    }

    /**
     * This function sets the onClickListener for the add button
     *
     * @param layout FragmentStockDetailBinding - This is the binding object for the fragment.
     */
    private fun setButtonListener(layout: FragmentStockDetailBinding){
        layout.stockDetailAdd.setOnClickListener {
            /* This is the code that is executed when the user clicks the add button. It checks if the
            user has entered all the required information and if not it displays a snackbar with the
            error message. If the user has entered all the required information it creates a new
            stock object and updates the stock in the database. */
            val id = args.stock.id
            val uid = args.stock.uid
            val branch = args.stock.branch
            val name = layout.stockDetailName.text.toString()
            val weight = layout.editDetailWeight.text.toString()
            val price = layout.priceDetail.text.toString()
            val unit = layout.unitDetail.text.toString()
            val max = layout.stockDetailQuantity.value
            val inStock = layout.stockDetailQuantity2.value
            when {
                name.isEmpty() -> {
                    view?.snack(R.string.iname)
                }
                name.length > 15 -> {
                    view?.snack(R.string.charsmax)
                }
                weight.isEmpty() -> {
                    view?.snack(R.string.iweight)
                }
                weight.length > 6 -> {
                    view?.snack(R.string.noweight)
                }
                price.isEmpty() -> {
                    view?.snack(R.string.iprice)
                }
                unit == "Unit" || unit.isEmpty() -> {
                    view?.snack(R.string.iunit)
                }
                inStock < 0 -> {
                    view?.snack(R.string.charsmin)
                }
                max <= 0 -> {
                    view?.snack(R.string.stock_lvl)
                }
                max < inStock -> {
                    view?.snack(R.string.stock_lvlmax)
                }
                else -> {
                    if(fav == null){
                        fav = false
                    }

                    val stock = StockModel(
                        id = id,
                        uid = uid,
                        name = name,
                        branch = branch,
                        weight = weight,
                        unit = unit,
                        price = price.toDouble(),
                        max = max,
                        inStock = current,
                    )
                    stockDetailViewModel.updateStock(uid, stock.id,stock)
                    val action =
                        StockDetailFragmentDirections.actionStockDetailFragmentToStockListFragment(stock.branch)
                    findNavController().navigate(action)
                }
            }
        }
    }


    override fun onResume() {
        setButtonListener(fragBinding)
        stockDetailViewModel.getStock(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.stock.id)
        val units = resources.getStringArray(R.array.units)
        val unitAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, units)
        fragBinding.unitDetail.setAdapter(unitAdapter)

        super.onResume()
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,
            requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    companion object


}