package org.wit.inventorymanager.ui.building

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.wit.inventorymanager.firebase.FirebaseImageManager
import org.wit.inventorymanager.models.BuildingManager
import org.wit.inventorymanager.models.BuildingModel
import timber.log.Timber
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.RuntimeException

class BuildingViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status


    fun addBuilding(firebaseUser: MutableLiveData<FirebaseUser>,building: BuildingModel) {
        status.value = try {
            building.image = FirebaseImageManager.imageUri.value.toString()
            BuildingManager.create(firebaseUser,building)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }


}