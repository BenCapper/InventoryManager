package org.wit.inventorymanager.fragments


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import splitties.snackbar.snack
import com.squareup.picasso.Picasso
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.FragmentStockBinding
import org.wit.inventorymanager.helpers.showImagePicker
import org.wit.inventorymanager.main.InventoryApp
import org.wit.inventorymanager.models.StockModel
import timber.log.Timber
import java.util.*


private var _fragBinding: FragmentStockBinding? = null
private val fragBinding get() = _fragBinding!!
private var stock = StockModel()
private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>

class StockFragment : Fragment() {

    lateinit var app: InventoryApp
    private var id: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as InventoryApp
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentStockBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setButtonListener(fragBinding)
        activity?.title = getString(R.string.action_location)
        registerImagePickerCallback()
        val bundle = arguments
        if (arguments?.isEmpty == false) {
            stock = bundle?.getParcelable("stock")!!


            if (stock.id !== (0).toLong()){
                id = stock.id
                fragBinding.btnAddItem.setText(R.string.up_loc)
            }
            if (stock.name != ""){
                fragBinding.stockName.setText(stock.name)
            }
            if (stock.price.toString() != ""){
                fragBinding.stockPrice.setText(stock.price.toString())
            }
            if (stock.weight != ""){
                fragBinding.stockWeight.setText(stock.weight)
            }
            if (stock.image != ""){
                Picasso.get()
                    .load(Uri.parse(stock.image))
                    .into(fragBinding.stockImage)
                fragBinding.chooseImage.setText(R.string.img_ch)
            }
            else{
                fragBinding.stockImage.setImageURI(null)
            }

        }


        fragBinding.stockImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }


        return root
    }

    override fun onPause() {

        super.onPause()
    }

    override fun onResume() {
        setButtonListener(fragBinding)
        if (stock.image != ""){
            Picasso.get()
                .load(Uri.parse(stock.image))
                .into(fragBinding.stockImage)
            fragBinding.chooseImage.setText(R.string.img_ch)
        }
        super.onResume()
    }

    private fun setButtonListener(layout: FragmentStockBinding) {
        layout.btnAddItem.setOnClickListener {

            stock.name = layout.stockName.text.toString()
            stock.price = layout.stockPrice.text.toString().toDouble()
            stock.weight = layout.stockWeight.text.toString()
            stock.inStock = layout.quantity.value.toLong()

            if (stock.name.isEmpty()) {
                view?.snack(R.string.s_name)
            } else if (stock.name.length > 15){
                view?.snack(R.string.s_name_chars)
            } else if (stock.price == 0.0) {
                view?.snack(R.string.s_price)
            } else if (stock.weight.isEmpty()) {
                view?.snack(R.string.s_weight)
            } else if (stock.image == "") {
                view?.snack(R.string.loc_img)
            }
            else {
                if (stock.id.toString().length == 1){
                    stock.id = Random().nextLong()
                    app.stocks.create(stock)
                    view?.snack(R.string.s_create)
                }
                else {
                    app.stocks.update(stock)
                    view?.snack(R.string.s_update)
                }
                Timber.i(stock.toString())

                layout.stockName.setText("")
                layout.stockPrice.setText("")
                layout.stockWeight.setText("")
                layout.stockImage.setImageURI(null)
                stock.name = ""
                stock.price = 0.0
                stock.weight = ""
                stock.image = ""
                stock.inStock = 0
                stock.id = 0


                it.findNavController()
                    .navigate(R.id.action_stockFragment_to_stockListFragment)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,
            requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${result.data!!.data}")
                            stock.image = result.data!!.data!!.toString()
                            Picasso.get()
                                .load(stock.image)
                                .into(fragBinding.stockImage)
                            fragBinding.chooseImage.setText(R.string.change_stock_image)
                        }
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            StockFragment().apply {
                arguments = Bundle().apply {}
            }
    }

}