package org.wit.inventorymanager.ui.buildingDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wit.inventorymanager.models.BuildingManager
import org.wit.inventorymanager.models.BuildingModel
import timber.log.Timber
import java.lang.Exception

class BuildingDetailViewModel : ViewModel() {

    private val build = MutableLiveData<BuildingModel>()

    var observableBuild: LiveData<BuildingModel>
        get() = build
        set(value) {build.value = value.value}


    fun getBuild(id: String) {
        try {
            //DonationManager.findById(email, id, donation)
            BuildingManager.buildingById(id, build)
            Timber.i("Detail getBuild() Success : ${
                build.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Detail getBuild() Error : $e.message")
        }
    }

    fun updateBuild(id: String,build: BuildingModel) {
        try {
            BuildingManager.update(id, build)
            Timber.i("Detail update() Success : $build")
        } catch (e: Exception) {
            Timber.i("Detail update() Error : $e.message")
        }
    }
}