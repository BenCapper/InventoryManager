package org.wit.inventorymanager.ui.buildingDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wit.inventorymanager.firebase.BuildingManager
import org.wit.inventorymanager.models.BuildingModel
import timber.log.Timber
import java.lang.Exception

class BuildingDetailViewModel : ViewModel() {

    private val build = MutableLiveData<BuildingModel>()

    var observableBuild: LiveData<BuildingModel>
        get() = build
        set(value) {build.value = value.value}


    /**
     * It gets the building with the given userid and id.
     *
     * @param userid The user's id.
     * @param id The id of the building you want to get.
     */
    fun getBuild(userid: String, id: String) {
        try {
            BuildingManager.findById(userid, id, build)
            Timber.i("Detail getBuild() Success : ${
                build.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Detail getBuild() Error : $e.message")
        }
    }

    /**
     * It updates the building with the given id.
     *
     * @param userid The user's ID.
     * @param id The id of the building you want to update.
     * @param build BuildingModel
     */
    fun updateBuild(userid: String, id: String,build: BuildingModel) {
        try {
            BuildingManager.update(userid,id, build)
            Timber.i("Detail update() Success : $build")
        } catch (e: Exception) {
            Timber.i("Detail update() Error : $e.message")
        }
    }
}