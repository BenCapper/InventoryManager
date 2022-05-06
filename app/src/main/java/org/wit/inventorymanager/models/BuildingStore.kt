package org.wit.inventorymanager.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface BuildingStore {
    fun findAll(buildingList:
                MutableLiveData<List<BuildingModel>>)
    fun findAll(userid:String,
                buildingList:
                MutableLiveData<List<BuildingModel>>)
    fun findById(userid:String, buildingid: String,
                 donation: MutableLiveData<BuildingModel>)
    fun create(firebaseUser: MutableLiveData<FirebaseUser>, building: BuildingModel)
    fun delete(userid:String, buildingid: String)
    fun update(userid:String, buildingid: String, building: BuildingModel)
    fun search(userid: String,term: String, buildingList: MutableLiveData<List<BuildingModel>>)
}