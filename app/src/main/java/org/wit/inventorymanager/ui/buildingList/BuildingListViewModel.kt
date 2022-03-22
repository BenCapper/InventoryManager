package org.wit.inventorymanager.ui.buildingList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wit.inventorymanager.models.BuildingManager
import org.wit.inventorymanager.models.BuildingModel

class BuildingListViewModel : ViewModel() {

    private val buildingList = MutableLiveData<List<BuildingModel>>()

    val observableBuildingList: LiveData<List<BuildingModel>>
        get() = buildingList

    private val status = MutableLiveData<Boolean>()


    init {
        load()
    }

    fun deleteBuilding(building: BuildingModel) {
        status.value = try {
            BuildingManager.delete(building)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    private fun load() {
        buildingList.value = BuildingManager.findAll(buildingList)
    }

}