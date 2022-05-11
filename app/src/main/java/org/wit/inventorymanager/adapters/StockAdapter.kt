package org.wit.inventorymanager.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wit.inventorymanager.databinding.CardStockBinding
import org.wit.inventorymanager.models.StockModel
import java.util.*


/* This is an interface that defines the methods that will be called when the user clicks on the
add/minus buttons, swipes to edit, clicks on the favorite button, or clicks on the stock itself. */
interface StockListener {
    fun onAddStockClick(stock: StockModel)
    fun onMinusStockClick(stock: StockModel)
    fun onEditSwipe(stock: StockModel)
    fun onFave(stock: StockModel)
    fun onStockClick(stock: StockModel)
}

class StockAdapter constructor(private var stock: ArrayList<StockModel>, private val listener: StockListener)
    : RecyclerView.Adapter<StockAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardStockBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    /**
     * Removes an item from the list at a given position.
     *
     * @param position The position of the item in the list.
     */
    fun removeAt(position: Int) {
        stock.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * It binds the data to the view holder.
     *
     * @param holder MainHolder - this is the view holder that will be used to display the data.
     * @param position Int - The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val stock = stock[holder.absoluteAdapterPosition]
        holder.bind(stock, listener)
    }

    /**
     * The function returns the size of the stock list.
     */
    override fun getItemCount(): Int = stock.size


    inner class MainHolder(private val binding : CardStockBinding) : RecyclerView.ViewHolder(binding.root) {


        /**
         * It binds the data to the view.
         *
         * @param stock StockModel - This is the data that will be used to populate the view.
         * @param listener This is the listener that will be called when the user clicks on the add or
         * minus button.
         */
        fun bind(stock: StockModel, listener : StockListener) {

            binding.root.tag = stock
            binding.stock = stock
            binding.btnup.setOnClickListener { listener.onAddStockClick(stock) }
            binding.btndown.setOnClickListener { listener.onMinusStockClick(stock) }
            binding.executePendingBindings()
        }
    }
}

