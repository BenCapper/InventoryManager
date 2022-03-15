package org.wit.inventorymanager.ui.building

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wit.inventorymanager.models.BuildingManager
import org.wit.inventorymanager.models.BuildingModel

class BuildingViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

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