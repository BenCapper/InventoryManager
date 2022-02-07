package org.wit.inventorymanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.CardBuildingBinding
import org.wit.inventorymanager.helpers.readImageFromPath
import org.wit.inventorymanager.models.BuildingModel

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
        val building = buildings[holder.adapterPosition]
        holder.bind(building, listener)
    }

    override fun getItemCount(): Int = buildings.size

    inner class MainHolder(val binding : CardBuildingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(building: BuildingModel, listener : BuildingListener) {
            binding.buildingName.text = building.name
            binding.address.text= building.address
            binding.phone.text = building.phone
            binding.imageIcon.setImageBitmap(readImageFromPath(itemView.context, building.image))
            binding.edit.setOnClickListener { listener.onEditBuildingClick(building)}
        }
    }
}
