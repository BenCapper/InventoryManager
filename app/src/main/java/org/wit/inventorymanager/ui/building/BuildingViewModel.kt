package org.wit.inventorymanager.ui.building

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.wit.inventorymanager.models.BuildingManager
import org.wit.inventorymanager.models.BuildingModel


class BuildingViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status


    fun addBuilding(firebaseUser: MutableLiveData<FirebaseUser>,building: BuildingModel) {
        status.value = try {
            BuildingManager.create(firebaseUser,building)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }


}