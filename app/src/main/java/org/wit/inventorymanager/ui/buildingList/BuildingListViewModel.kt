package org.wit.inventorymanager.ui.buildingList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.wit.inventorymanager.models.BuildingManager
import org.wit.inventorymanager.models.BuildingModel
import timber.log.Timber
import java.lang.Exception

class BuildingListViewModel : ViewModel() {

    private val buildingList =
        MutableLiveData<List<BuildingModel>>()

    val observableBuildingList: LiveData<List<BuildingModel>>
        get() = buildingList

    private val build = MutableLiveData<BuildingModel>()

    var observableBuild: LiveData<BuildingModel>
        get() = build
        set(value) {build.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    var readOnly = MutableLiveData(false)

    var searchResults = ArrayList<BuildingModel>()

    init { load() }

    fun load() {
        try {
            readOnly.value = false
            BuildingManager.findAll(liveFirebaseUser.value?.uid!!,
                buildingList)
            Timber.i("Report Load Success : ${buildingList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }

    fun loadAll() {
        try {
            readOnly.value = true
            BuildingManager.findAll(buildingList)
            Timber.i("Report LoadAll Success : ${buildingList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report LoadAll Error : $e.message")
        }
    }

    fun delete(userid: String, id: String) {
        try {
            BuildingManager.delete(userid,id)
            Timber.i("Building Delete Success")
        }
        catch (e: Exception) {
            Timber.i("Building Delete Error : $e.message")
        }
    }

    fun update(userid: String, id: String, build:BuildingModel) {
        try {
            BuildingManager.update(userid,id, build)
            Timber.i("Detail update() Success : $build")
        } catch (e: Exception) {
            Timber.i("Detail update() Error : $e.message")
        }
    }
}