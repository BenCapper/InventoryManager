package org.wit.inventorymanager.models

import androidx.lifecycle.MutableLiveData

interface BuildingStore {
    fun findAll(buildingList: MutableLiveData<List<BuildingModel>>): List<BuildingModel>
    fun create(building: BuildingModel)
    fun update(id: String, building: BuildingModel)
    fun delete(building: BuildingModel)
    fun filterBuildings(buildingName: String): List<BuildingModel>
    fun filterById(id: Long): List <BuildingModel>
    fun buildingById(id: String,building: MutableLiveData<BuildingModel>)
}