package org.wit.inventorymanager.models


import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import timber.log.Timber
import java.util.*


object BuildingManager : BuildingStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun findAll(buildingList: MutableLiveData<List<BuildingModel>>) {
        database.child("buildings")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase building error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<BuildingModel>()
                    val children = snapshot.children
                    children.forEach {
                        val building = it.getValue(BuildingModel::class.java)
                        localList.add(building!!)
                    }
                    database.child("buildings")
                        .removeEventListener(this)

                    buildingList.value = localList
                }
            })
    }

    override fun findAll(userid: String, buildingList: MutableLiveData<List<BuildingModel>>) {

        database.child("user-buildings").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase building error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<BuildingModel>()
                    val children = snapshot.children
                    children.forEach {
                        val building = it.getValue(BuildingModel::class.java)
                        localList.add(building!!)
                    }
                    database.child("user-buildings").child(userid)
                        .removeEventListener(this)

                    buildingList.value = localList
                }
            })
    }

    override fun search(userid: String,term: String, buildingList: MutableLiveData<List<BuildingModel>>) {

        database.child("user-buildings").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase building error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<BuildingModel>()
                    val children = snapshot.children
                    children.forEach {
                        if (it.getValue(BuildingModel::class.java)?.name!!.contains(term) ) {
                            val building = it.getValue(BuildingModel::class.java)
                            localList.add(building!!)
                        }
                    }
                    database.child("user-buildings").child(userid)
                        .removeEventListener(this)

                    buildingList.value = localList
                }
            })
    }

    override fun findById(userid: String, buildingid: String, building: MutableLiveData<BuildingModel>) {

        database.child("user-buildings").child(userid)
            .child(buildingid).get().addOnSuccessListener {
                building.value = it.getValue(BuildingModel::class.java)
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener{
                Timber.e("firebase Error getting data $it")
            }
    }

    override fun create(firebaseUser: MutableLiveData<FirebaseUser>, building: BuildingModel) {
        Timber.i("Firebase DB Reference : $database")

        val uid = firebaseUser.value!!.uid
        val key = database.child("building").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        building.uid = key
        val buildingValues = building.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/buildings/$key"] = buildingValues
        childAdd["/user-buildings/$uid/$key"] = buildingValues

        database.updateChildren(childAdd)
    }

    override fun delete(userid: String, buildingid: String) {

        val childDelete : MutableMap<String, Any?> = HashMap()
        childDelete["/buildings/$buildingid"] = null
        childDelete["/user-buildings/$userid/$buildingid"] = null

        database.updateChildren(childDelete)
    }

    override fun update(userid: String, buildingid: String, building: BuildingModel) {

        val buildingValues = building.toMap()

        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["buildings/$buildingid"] = buildingValues
        childUpdate["user-buildings/$userid/$buildingid"] = buildingValues

        database.updateChildren(childUpdate)
    }


    fun updateImageRef(userid: String,imageUri: String) {

        val userBuildings = database.child("user-buildings").child(userid)
        val allBuildings = database.child("buildings")

        userBuildings.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        //Update Users imageUri
                        it.ref.child("profilepic").setValue(imageUri)
                        //Update all donations that match 'it'
                        val building = it.getValue(BuildingModel::class.java)
                        allBuildings.child(building!!.uid!!)
                            .child("profilepic").setValue(imageUri)
                    }
                }
            })
    }

}