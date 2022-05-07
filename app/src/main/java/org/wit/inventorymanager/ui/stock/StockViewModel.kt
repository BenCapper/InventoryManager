package org.wit.inventorymanager.ui.stock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.wit.inventorymanager.models.BuildingManager
import org.wit.inventorymanager.models.BuildingModel
import org.wit.inventorymanager.models.StockManager
import org.wit.inventorymanager.models.StockModel

class StockViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

    fun addStock(firebaseUser: MutableLiveData<FirebaseUser>, stock: StockModel) {
        status.value = try {
            StockManager.create(firebaseUser,stock)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}