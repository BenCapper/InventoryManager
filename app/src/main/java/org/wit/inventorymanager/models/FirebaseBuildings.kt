package org.wit.inventorymanager.models

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.util.*

class FirebaseBuildings : BuildingStore{

    var buildings = mutableListOf<BuildingModel>()
    val db = FirebaseDatabase.getInstance("https://invmanage-4bcbd-default-rtdb.firebaseio.com")
        .getReference("Building")


    override fun findAll(): MutableList<BuildingModel> {
        return buildings
    }


    override fun create(building: BuildingModel) {
        db.child(building.id.toString()).setValue(building)
        Timber.i(db.toString())
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