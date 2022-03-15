package org.wit.inventorymanager.models


import android.view.View
import android.widget.Button
import androidx.navigation.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.wit.inventorymanager.R
import timber.log.Timber
import java.util.*


object BuildingManager : BuildingStore{

    var buildings = mutableListOf<BuildingModel>()
    private val db = FirebaseDatabase.getInstance("https://invmanage-4bcbd-default-rtdb.firebaseio.com")
        .getReference("Building")



    override fun findAll(): MutableList<BuildingModel> {
        db.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                buildings = mutableListOf()
                if(snapshot.exists()){
                    for(buildSnap in snapshot.children){
                        val build = buildSnap.getValue(BuildingModel::class.java)
                        buildings.add(build!!)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Timber.i("Failed: ${error.message}")
            }
        })
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
        return buildings.filter { b -> b.name.lowercase(Locale.getDefault()).contains(buildingName.lowercase(
            Locale.getDefault()
        )) }
    }

    override fun filterById(id: Long): List<BuildingModel> {
        return buildings.filter {b -> b.id == id }
    }

}