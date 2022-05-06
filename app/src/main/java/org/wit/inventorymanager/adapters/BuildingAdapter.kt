package org.wit.inventorymanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import org.wit.inventorymanager.databinding.CardBuildingBinding
import org.wit.inventorymanager.helpers.customTransformation
import org.wit.inventorymanager.models.BuildingModel
import java.util.*

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

    fun removeAt(position: Int) {
        buildings.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val building = buildings[holder.absoluteAdapterPosition]
        holder.bind(building, listener)
    }

    override fun getItemCount(): Int = buildings.size


    inner class MainHolder(private val binding : CardBuildingBinding, private val readOnly : Boolean) : RecyclerView.ViewHolder(binding.root) {

        val readOnlyRow = readOnly

        fun bind(building: BuildingModel, listener : BuildingListener) {
            /*Bind building information to the recyclerview card
            * Capitalise the first letter of the name and address
            * Load the image into the imageView
            * Building details act as links to that buildings stock */
            binding.root.tag = building
            binding.building = building
            binding.fave.setOnClickListener { listener.onFave(building) }
            binding.root.setOnClickListener { listener.onBuildingClick(building) }
            binding.executePendingBindings()
        }
    }
}
