package org.wit.inventorymanager.firebase


import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.BuildingStore
import timber.log.Timber
import java.util.*


object BuildingManager : BuildingStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    /**
     * We're using the Firebase database to get a list of buildings, and then we're adding a listener
     * to the database to get the data
     *
     * @param buildingList MutableLiveData<List<BuildingModel>>
     */
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

    /**
     * We're using the Firebase database to get a list of buildings that belong to a user
     *
     * @param userid The user's id
     * @param buildingList MutableLiveData<List<BuildingModel>>
     */
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

    /**
     * We are adding a listener to the database, and when the data changes, we are adding the data to a
     * local list, and then setting the value of the MutableLiveData object to the local list
     *
     * @param userid The user's id
     * @param term The search term
     * @param buildingList MutableLiveData<List<BuildingModel>>
     */
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

    /**
     * We're getting the building from the database by using the userid and buildingid
     *
     * @param userid The user's id
     * @param buildingid The id of the building you want to get
     * @param building MutableLiveData<BuildingModel>
     */
    override fun findById(userid: String, buildingid: String, building: MutableLiveData<BuildingModel>) {

        database.child("user-buildings").child(userid)
            .child(buildingid).get().addOnSuccessListener {
                building.value = it.getValue(BuildingModel::class.java)
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener{
                Timber.e("firebase Error getting data $it")
            }
    }

    /**
     * We create a new building in the database by creating a new key, adding the building to the
     * buildings table, and adding the building to the user-buildings table
     *
     * @param firebaseUser MutableLiveData<FirebaseUser>
     * @param building BuildingModel
     * @return A HashMap of the building values.
     */
    override fun create(firebaseUser: MutableLiveData<FirebaseUser>, building: BuildingModel) {
        Timber.i("Firebase DB Reference : $database")

        val uid = firebaseUser.value!!.uid
        val key = database.child("building").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        building.id = key
        val buildingValues = building.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/buildings/$key"] = buildingValues
        childAdd["/user-buildings/$uid/$key"] = buildingValues

        database.updateChildren(childAdd)
    }

    /**
     * This function deletes a building from the database
     *
     * @param userid The user's id
     * @param buildingid The id of the building to be deleted
     */
    override fun delete(userid: String, buildingid: String) {

        val childDelete : MutableMap<String, Any?> = HashMap()
        childDelete["/buildings/$buildingid"] = null
        childDelete["/user-buildings/$userid/$buildingid"] = null

        database.updateChildren(childDelete)
    }

    /**
     * We create a map of the building values, then create a map of the building values and the
     * building id, then update the database with the child update map
     *
     * @param userid The userid of the user who owns the building
     * @param buildingid The id of the building to update
     * @param building BuildingModel - this is the building object that we want to update
     */
    override fun update(userid: String, buildingid: String, building: BuildingModel) {

        val buildingValues = building.toMap()

        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["buildings/$buildingid"] = buildingValues
        childUpdate["user-buildings/$userid/$buildingid"] = buildingValues

        database.updateChildren(childUpdate)
    }


    /**
     * > Update the profile picture of all buildings that the user has donated to
     *
     * @param userid The user's id
     * @param imageUri The imageUri that you want to update the image to.
     */
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
                        allBuildings.child(building!!.uid)
                            .child("profilepic").setValue(imageUri)
                    }
                }
            })
    }

}