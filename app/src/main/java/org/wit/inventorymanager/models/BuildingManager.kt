package org.wit.inventorymanager.models


import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import java.util.*


object BuildingManager : BuildingStore {

    var buildings = mutableListOf<BuildingModel>()
    private val db =
        FirebaseDatabase.getInstance("https://invmanage-4bcbd-default-rtdb.firebaseio.com")
            .getReference("Building")


    override fun findAll(buildingList: MutableLiveData<List<BuildingModel>>): List<BuildingModel> {
        val localList = ArrayList<BuildingModel>()
        db.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Timber.i("Firebase Donation error : ${error.message}")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                children.forEach {
                    val building = it.getValue(BuildingModel::class.java)
                    localList.add(building!!)
                }
                db.removeEventListener(this)

                buildingList.value = localList
            }
        })
        return localList
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
        return buildings.filter { b ->
            b.name.lowercase(Locale.getDefault()).contains(
                buildingName.lowercase(
                    Locale.getDefault()
                )
            )
        }
    }

    override fun filterById(id: Long): List<BuildingModel> {
        return buildings.filter { b -> b.id == id }
    }

    override fun buildingById(id: Long): BuildingModel? {
        return buildings.find { it.id == id }
    }
}