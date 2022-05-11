package org.wit.inventorymanager.ui.stock


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
import org.wit.inventorymanager.databinding.FragmentStockBinding
import org.wit.inventorymanager.models.StockModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import splitties.snackbar.snack
import kotlin.random.Random


class StockFragment : Fragment() {

    private var nFragBinding: FragmentStockBinding? = null
    private val fragBinding get() = nFragBinding!!
    private lateinit var stockViewModel: StockViewModel
    private val args by navArgs<StockFragmentArgs>()
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

        nFragBinding = FragmentStockBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        activity?.title = getString(R.string.action_stock)

        stockViewModel = ViewModelProvider(this)[StockViewModel::class.java]
        stockViewModel.observableStatus.observe(viewLifecycleOwner) { status ->
            status?.let { render(status) }
        }
        val units = resources.getStringArray(R.array.units)
        val unitAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, units)
        fragBinding.unit.setAdapter(unitAdapter)


        /* This is setting the minimum and maximum values for the number pickers. */
        fragBinding.stockQuantity.minValue = 0
        fragBinding.stockQuantity.maxValue = 1000

        fragBinding.stockQuantity2.minValue = 0
        fragBinding.stockQuantity2.maxValue = 1000

        // Number picker listener
        fragBinding.stockQuantity.setOnValueChangedListener { _, _, newVal ->
            max = newVal
        }

        fragBinding.stockQuantity2.setOnValueChangedListener { _, _, newVal ->
            current = newVal
        }


        setButtonListener(fragBinding)

        return root
    }


    /**
     * This function is used to set the onClickListener for the add button
     *
     * @param layout FragmentStockBinding - This is the binding variable that is used to access the
     * layout.
     */
    private fun setButtonListener(layout: FragmentStockBinding){
        /* This is the code that is executed when the user clicks the add button. It is checking if the
        user has entered all the required information and if they have it will add the stock to the
        database. */
        layout.stockAdd.setOnClickListener {
            val name = layout.stockName.text.toString()
            val branch = args.buildingid
            val weight = layout.editWeight.text.toString()
            val price = layout.price.text.toString()
            val unit = layout.unit.text.toString()
            val max = layout.stockQuantity.value
            val inStock = layout.stockQuantity2.value
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
                        id = Random.nextLong().toString(),
                        uid = loggedInViewModel.liveFirebaseUser.value?.uid!!,
                        name = name,
                        branch = branch,
                        weight = weight,
                        price = price.toDouble(),
                        max = max,
                        unit = unit,
                        inStock = current,
                    )

                    stockViewModel.addStock(loggedInViewModel.liveFirebaseUser, stock)
                    val action =
                        StockFragmentDirections.actionStockFragmentToStockListFragment(branch)
                    findNavController().navigate(action)
                }
            }
        }
    }

    /**
     * > If the status is true, do something with the view, otherwise show a snackbar with the message
     * "Failed"
     *
     * @param status Boolean
     */
    private fun render(status: Boolean) {
        when (status) {
            true -> {
                view?.let {
                }
            }
            false -> view?.snack("Failed")
        }
    }


    override fun onResume() {
        setButtonListener(fragBinding)
        val units = resources.getStringArray(R.array.units)
        val unitAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, units)
        fragBinding.unit.setAdapter(unitAdapter)
        super.onResume()
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,
            requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    companion object {
    }


}