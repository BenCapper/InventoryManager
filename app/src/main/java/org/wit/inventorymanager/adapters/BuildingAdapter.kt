package org.wit.inventorymanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wit.inventorymanager.databinding.CardBuildingBinding
import org.wit.inventorymanager.models.BuildingModel
import java.util.*


/* This is an interface that defines the methods that will be called when a building is clicked, when a
building is swiped to edit, and when a building is favourite. */
interface BuildingListener {
    fun onBuildingClick(building: BuildingModel)
    fun onEditSwipe(building: BuildingModel)
    fun onFave(building: BuildingModel)
}

class BuildingAdapter constructor(private var buildings: ArrayList<BuildingModel>, private val listener: BuildingListener, private val readOnly: Boolean)
    : RecyclerView.Adapter<BuildingAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardBuildingBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding, readOnly)
    }

    /**
     * Removes the item at the given position from the list and notifies the adapter that the item has
     * been removed.
     *
     * @param position The position of the item in the list.
     */
    fun removeAt(position: Int) {
        buildings.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * `onBindViewHolder` is a function that takes a `MainHolder` and an `Int` as parameters and
     * returns nothing
     *
     * @param holder MainHolder - this is the view holder that will be used to display the data.
     * @param position Int - The position of the item in the adapter's data set.
     */
    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val building = buildings[holder.absoluteAdapterPosition]
        holder.bind(building, listener)
    }

    /**
     * The function returns the number of items in the buildings list.
     */
    override fun getItemCount(): Int = buildings.size


    /* This class binds the building information to the recyclerview card */
    inner class MainHolder(private val binding : CardBuildingBinding, readOnly : Boolean) : RecyclerView.ViewHolder(binding.root) {

        val readOnlyRow = readOnly

        /**
         * `bind` is a function that takes a `BuildingModel` and a `BuildingListener` and sets the
         * `BuildingModel` as the `tag` of the `root` view, sets the `BuildingModel` as the `building`
         * of the `binding`, sets the `onClickListener` of the `fave` view to the `onFave` function of
         * the `BuildingListener`, and sets the `onClickListener` of the `root` view to the
         * `onBuildingClick` function of the `BuildingListener`
         *
         * @param building BuildingModel - this is the data that we want to bind to the view.
         * @param listener BuildingListener - this is the interface we created in the previous step.
         */
        fun bind(building: BuildingModel, listener : BuildingListener) {
            binding.root.tag = building
            binding.building = building
            binding.fave.setOnClickListener { listener.onFave(building) }
            binding.root.setOnClickListener { listener.onBuildingClick(building) }
            binding.executePendingBindings()
        }
    }
}
