package org.wit.inventorymanager.ui.stock


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
import org.wit.inventorymanager.main.InventoryApp
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.StockModel
import timber.log.Timber
import java.util.*


private var nFragBinding: FragmentStockBinding? = null
private val fragBinding get() = nFragBinding!!
private var stock = StockModel()
private var build = BuildingModel()
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
    ): View {

        nFragBinding = FragmentStockBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setButtonListener(fragBinding)
        activity?.title = getString(R.string.action_location)
        registerImagePickerCallback()
        fragBinding.quantity.minValue = 1
        fragBinding.quantity.maxValue = 2000

        // Number picker listener
        fragBinding.quantity.setOnValueChangedListener { _, _, newVal ->
            stock.inStock = newVal.toLong()
        }

        // Check for arguments and save / set fields and text views
        val bundle = arguments
        if (arguments?.containsKey("stock") == true){
            stock = bundle?.getParcelable("stock")!!
        }
        if(arguments?.containsKey("build") == true) {
            build = bundle?.getParcelable("build")!!
        }

            if (stock.id.toString() !== (0).toLong().toString()){
                id = stock.id
                fragBinding.btnAddItem.setText(R.string.up_stock)
            }
            if (stock.name != ""){
                fragBinding.stockName.setText(stock.name)
            }
            if (stock.price.toString() != "0.0"){
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


        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }


        return root
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
            if(!layout.stockPrice.text.isNullOrEmpty()) {
                stock.price = layout.stockPrice.text.toString().toDouble()
            }
            stock.weight = layout.stockWeight.text.toString()
            stock.inStock = layout.quantity.value.toLong()
            stock.branch = build.id

            // Input validation
            when {
                stock.name.isEmpty() -> {
                    view?.snack(R.string.s_name)
                }
                stock.name.length > 20 -> {
                    view?.snack(R.string.s_name_chars)
                }
                stock.price == 0.0 -> {
                    view?.snack(R.string.s_price)
                }
                stock.weight.isEmpty() -> {
                    view?.snack(R.string.s_weight)
                }
                stock.image == "" -> {
                    view?.snack(R.string.stock_img)
                }
                else -> {

                    if (stock.id.toString().length == 1){
                        stock.id = Random().nextLong()
                        app.stocks.create(stock)
                        view?.snack(R.string.s_create)
                    } else {
                        app.stocks.update(stock)
                        view?.snack(R.string.s_update)
                    }
                    Timber.i(stock.toString())
                    val bundle = Bundle()
                    bundle.putParcelable("id", build)
                    bundle.putParcelable("stock", stock)
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
                    stock.branch = 0


                    it.findNavController()
                        .navigate(R.id.action_stockFragment_to_stockListFragment, bundle)
                }
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
    private fun showImagePicker(intentLauncher: ActivityResultLauncher<Intent>) {

        var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
        chooseFile.type = "image/*"
        chooseFile = Intent.createChooser(chooseFile, R.string.button_addImage.toString())
        intentLauncher.launch(chooseFile)
    }
    }


