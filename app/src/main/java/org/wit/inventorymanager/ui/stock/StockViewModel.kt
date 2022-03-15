package org.wit.inventorymanager.ui.stock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wit.inventorymanager.models.StockManager
import org.wit.inventorymanager.models.StockModel

class StockViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

    fun addStock(stock: StockModel) {
        status.value = try {
            StockManager.create(stock)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun updateStock(stock: StockModel) {
        status.value = try {
            StockManager.update(stock)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}