package org.wit.inventorymanager.ui.building

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wit.inventorymanager.models.BuildingManager
import org.wit.inventorymanager.models.BuildingModel
import timber.log.Timber
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.RuntimeException

class BuildingViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()
    private val build = MutableLiveData<BuildingModel>()

    val observableStatus: LiveData<Boolean>
        get() = status

    var observableBuild: LiveData<BuildingModel>
        get() = build
        set(value) {build.value = value.value}

    fun getBuild(id:Long): BuildingModel?  {
        try {
            build.value = BuildingManager.buildingById(id)
            Timber.i("Detail getBuild() Success : ${build.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Detail getBuild() Error : $e.message")
        }
        return build.value
    }

    fun addBuilding(building: BuildingModel) {
        status.value = try {
            BuildingManager.create(building)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun updateBuilding(building: BuildingModel) {
        status.value = try {
            BuildingManager.update(building)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}