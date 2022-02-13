package org.wit.inventorymanager.adapters


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.wit.inventorymanager.databinding.CardStockBinding
import org.wit.inventorymanager.models.StockModel
import splitties.snackbar.snack
import splitties.views.InputType
import java.math.RoundingMode
import java.util.*
import java.text.DecimalFormat
import java.text.NumberFormat


interface StockListener {
    fun onStockClick(stock: StockModel)
    fun onAddStockClick(stock: StockModel)
    fun onMinusStockClick(stock: StockModel)
}

class StockAdapter constructor(private var stocks: List<StockModel>, private val listener: StockListener)
    : RecyclerView.Adapter<StockAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardStockBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }



    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val stock = stocks[holder.absoluteAdapterPosition]
        holder.bind(stock, listener)
    }

    override fun getItemCount(): Int = stocks.size

    inner class MainHolder(private val binding : CardStockBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stock: StockModel, listener : StockListener) {
            /*Bind stock information to the recyclerview card
            * Capitalise the first letter of the name
            * Load the image into the imageView as an icon
            * Stock details act as links to edit that item
            * Quantity can be changed from card and amount displayed
            * When quantity = 0, set the details colour to red */

            binding.stockListName.text = stock.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
            val currencyForm = NumberFormat.getCurrencyInstance().format(stock.price)
            binding.stockListPrice.text = currencyForm
            binding.stockListWeight.text = stock.weight
            Picasso.get().load(stock.image).resize(200,200).into(binding.stockImageIcon)
            binding.stockListQuantity.text = stock.inStock.toString()
            if (stock.inStock == 0L){
                binding.stockListQuantity.setTextColor(Color.RED)
                binding.stockListWeight.setTextColor(Color.RED)
                binding.stockListPrice.setTextColor(Color.RED)
                binding.stockListName.setTextColor(Color.RED)
            }
            binding.stockListName.setOnClickListener { listener.onStockClick(stock) }
            binding.stockListPrice.setOnClickListener { listener.onStockClick(stock) }
            binding.stockListWeight.setOnClickListener { listener.onStockClick(stock) }
            binding.stockListQuantity.setOnClickListener { listener.onStockClick(stock) }
            binding.plus.setOnClickListener { listener.onAddStockClick(stock)}
            binding.minus.setOnClickListener {listener.onMinusStockClick(stock)}
        }
    }


}
