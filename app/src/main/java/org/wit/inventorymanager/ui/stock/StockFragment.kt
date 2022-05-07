package org.wit.inventorymanager.ui.stock


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import splitties.snackbar.snack
import com.squareup.picasso.Picasso
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentBuildingBinding
import org.wit.inventorymanager.databinding.FragmentStockBinding
import org.wit.inventorymanager.main.InventoryApp
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.StockModel
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.building.BuildingFragmentDirections
import org.wit.inventorymanager.ui.building.BuildingViewModel
import org.wit.inventorymanager.ui.buildingList.BuildingListViewModel
import org.wit.inventorymanager.ui.maps.MapsViewModel
import org.wit.inventorymanager.ui.stock.StockFragmentDirections.Companion.actionStockFragmentToStockListFragment
import org.wit.inventorymanager.ui.stockList.StockListFragmentArgs
import org.wit.inventorymanager.ui.stockList.StockListViewModel
import timber.log.Timber
import java.util.*
import kotlin.random.Random


class StockFragment : Fragment() {

    private var nFragBinding: FragmentStockBinding? = null
    private val fragBinding get() = nFragBinding!!
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var stockViewModel: StockViewModel
    private val args by navArgs<StockFragmentArgs>()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private var fav: Boolean? = null
    var max = 0
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

        fragBinding.sfave.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            fav = b
            if (fragBinding.sfave.isChecked){
                fragBinding.sfave.setTextColor(Color.argb(255,235, 172, 12))
            }
            else {
                fragBinding.sfave.setTextColor(Color.BLACK)
            }
        }
        fragBinding.stockQuantity.minValue = 0
        fragBinding.stockQuantity.maxValue = 10000

        fragBinding.stockQuantity2.minValue = 0
        fragBinding.stockQuantity2.maxValue = 10000

        // Number picker listener
        fragBinding.stockQuantity.setOnValueChangedListener { _, _, newVal ->
            max = newVal
        }

        fragBinding.stockQuantity.setOnValueChangedListener { _, _, newVal ->
            current = newVal
        }


        setButtonListener(fragBinding)

        return root
    }

    private fun setButtonListener(layout: FragmentStockBinding){
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
                else -> {
                    if(fav == null){
                        fav = false
                    }

                    var stock = StockModel(
                        id = Random.nextLong().toString(),
                        uid = loggedInViewModel.liveFirebaseUser.value?.uid!!,
                        name = name,
                        branch = branch,
                        weight = weight,
                        price = price.toDouble(),
                        max = max,
                        inStock = current,
                        image = "",
                        faved = fav!!
                    )
                    stockViewModel.addStock(loggedInViewModel.liveFirebaseUser, stock)
                    val action =
                        StockFragmentDirections.actionStockFragmentToStockListFragment("")
                    findNavController().navigate(action)
                }
            }
        }
    }

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
        @JvmStatic
        fun newInstance() =
            StockFragment().apply {
                arguments = Bundle().apply {}
            }
    }


}