package org.wit.inventorymanager.models

import com.google.firebase.database.FirebaseDatabase
import java.util.*

class FirebaseBuildings : BuildingStore{

    var buildings = mutableListOf<BuildingModel>()
    private val db = FirebaseDatabase.getInstance().reference.child("Building")


    override fun findAll(): MutableList<BuildingModel> {
        return buildings
    }


    fun generateRandomBuildId(): Long {
        return Random().nextLong()
    }
    override fun create(building: BuildingModel) {
        db.child(building.id.toString()).setValue(building)
    }


    override fun update(building: BuildingModel) {
        db.child(building.id.toString()).setValue(building)

    }

    override fun delete(building: BuildingModel) {
        db.child(building.id.toString()).removeValue()
    }

    override fun filterBuildings(buildingName: String): List<BuildingModel> {
        return buildings.filter { b -> b.name.toLowerCase().contains(buildingName.toLowerCase()) }
    }

}