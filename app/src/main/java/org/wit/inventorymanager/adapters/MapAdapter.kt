package org.wit.inventorymanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wit.inventorymanager.databinding.CardMapBinding
import org.wit.inventorymanager.models.BuildingModel
import java.util.ArrayList


class MapAdapter constructor(private var buildings: ArrayList<BuildingModel>, private val listener: BuildingListener)
    : RecyclerView.Adapter<MapAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardMapBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
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


    /* The MainHolder class is a RecyclerView.ViewHolder that binds a BuildingModel to a CardMapBinding */
    inner class MainHolder(private val binding : CardMapBinding) : RecyclerView.ViewHolder(binding.root) {


        /**
         * `bind` is a function that takes a `BuildingModel` and a `BuildingListener` and sets the
         * `BuildingModel` as the `tag` of the `root` view, sets the `BuildingModel` as the `building`
         * of the `binding`, sets the `onClickListener` of the `fave` view to the `onFave` function of
         * the `BuildingListener`, and sets the `onClickListener` of the `root` view to the
         * `onBuildingClick` function of the `BuildingListener`
         *
         * @param building BuildingModel - this is the data that will be bound to the view
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
