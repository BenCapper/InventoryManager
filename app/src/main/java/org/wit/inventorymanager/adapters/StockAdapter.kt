package org.wit.inventorymanager.adapters


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.wit.inventorymanager.databinding.CardBuildingBinding
import org.wit.inventorymanager.databinding.CardStockBinding
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.StockModel
import splitties.snackbar.snack
import splitties.views.InputType
import java.math.RoundingMode
import java.util.*
import java.text.DecimalFormat
import java.text.NumberFormat


interface StockListener {
    fun onAddStockClick(stock: StockModel)
    fun onMinusStockClick(stock: StockModel)
    fun onEditSwipe(stock: StockModel)
    fun onFave(stock: StockModel)
    fun onStockClick(stock: StockModel)
}

class StockAdapter constructor(private var stock: ArrayList<StockModel>, private val listener: StockListener, private val readOnly: Boolean)
    : RecyclerView.Adapter<StockAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardStockBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding, readOnly)
    }

    fun removeAt(position: Int) {
        stock.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val stock = stock[holder.absoluteAdapterPosition]
        holder.bind(stock, listener)
    }

    override fun getItemCount(): Int = stock.size


    inner class MainHolder(private val binding : CardStockBinding, private val readOnly : Boolean) : RecyclerView.ViewHolder(binding.root) {

        val readOnlyRow = readOnly

        fun bind(stock: StockModel, listener : StockListener) {
            /*Bind building information to the recyclerview card
            * Capitalise the first letter of the name and address
            * Load the image into the imageView
            * Building details act as links to that buildings stock */
            binding.root.tag = stock
            binding.stock = stock
            binding.btnup.setOnClickListener { listener.onAddStockClick(stock) }
            binding.btndown.setOnClickListener { listener.onMinusStockClick(stock) }
            binding.executePendingBindings()
        }
    }
}

