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


    inner class MainHolder(val binding : CardBuildingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(building: BuildingModel, listener : BuildingListener) {
            binding.buildingName.text = building.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
            binding.address.text= building.address.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
            binding.phone.text = building.phone
            Picasso.get().load(building.image).resize(200,200).into(binding.imageIcon)
            binding.edit.setOnClickListener { listener.onEditBuildingClick(building)}
            binding.buildingName.setOnClickListener { listener.onBuildingClick(building)}
            binding.imageIcon.setOnClickListener { listener.onBuildingClick(building)}
            binding.address.setOnClickListener { listener.onBuildingClick(building)}
            binding.phone.setOnClickListener { listener.onBuildingClick(building)}

        }
    }
}
