package org.wit.inventorymanager.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.CardBuildingBinding
import org.wit.inventorymanager.databinding.CardStockBinding
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.StockModel


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

    inner class MainHolder(val binding : CardStockBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stock: StockModel, listener : StockListener) {
            binding.stockListName.text = stock.name
            binding.stockListPrice.text = stock.price.toString()
            binding.stockListWeight.text = stock.weight
            Picasso.get().load(stock.image).resize(200,200).into(binding.stockImageIcon)


        }
    }


}
