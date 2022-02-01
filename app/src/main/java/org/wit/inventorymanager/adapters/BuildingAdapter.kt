package org.wit.inventorymanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.CardBuildingBinding
import org.wit.inventorymanager.helpers.readImageFromPath
import org.wit.inventorymanager.models.BuildingModel

class BuildingAdapter constructor(private var buildings: List<BuildingModel>)
    : RecyclerView.Adapter<BuildingAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardBuildingBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val building = buildings[holder.adapterPosition]
        holder.bind(building)
    }

    override fun getItemCount(): Int = buildings.size

    inner class MainHolder(val binding : CardBuildingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(building: BuildingModel) {
            binding.buildingName.text = building.name
            binding.address.text= building.address
            binding.imageIcon.setImageBitmap(readImageFromPath(itemView.context, building.image))
        }
    }
}
