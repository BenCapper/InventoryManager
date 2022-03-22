package org.wit.inventorymanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.wit.inventorymanager.databinding.CardBuildingBinding
import org.wit.inventorymanager.models.BuildingModel
import java.util.*

interface BuildingListener {
    fun onBuildingClick(building: BuildingModel)
    fun onEditBuildingClick(building: BuildingModel)
}

class BuildingAdapter constructor(private var buildings: List<BuildingModel>, private val listener: BuildingListener)
    : RecyclerView.Adapter<BuildingAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardBuildingBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }



    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val building = buildings[holder.absoluteAdapterPosition]
        holder.bind(building, listener)
    }

    override fun getItemCount(): Int = buildings.size


    inner class MainHolder(private val binding : CardBuildingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(building: BuildingModel, listener : BuildingListener) {
            /*Bind building information to the recyclerview card
            * Capitalise the first letter of the name and address
            * Load the image into the imageView
            * Building details act as links to that buildings stock */

            binding.building = building
            Picasso.get().load(building.image).resize(200,200).into(binding.imageIcon)
            binding.edit.setOnClickListener { listener.onEditBuildingClick(building)}
            binding.buildingName.setOnClickListener { listener.onBuildingClick(building)}
            binding.imageIcon.setOnClickListener { listener.onBuildingClick(building)}
            binding.address.setOnClickListener { listener.onBuildingClick(building)}
            binding.phone.setOnClickListener { listener.onBuildingClick(building)}
            binding.executePendingBindings()
        }
    }
}
