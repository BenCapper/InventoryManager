package org.wit.inventorymanager.adapters


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.wit.inventorymanager.databinding.CardStockBinding
import org.wit.inventorymanager.models.StockModel
import java.util.*


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
            binding.stockListName.text = stock.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
            binding.stockListPrice.text = stock.price.toString()
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
            binding.plus.setOnClickListener { listener.onAddStockClick(stock) }
            binding.minus.setOnClickListener {listener.onMinusStockClick(stock)}


        }
    }


}
