package org.wit.inventorymanager.ui.stockDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wit.inventorymanager.models.StockManager
import org.wit.inventorymanager.models.StockModel
import timber.log.Timber
import java.lang.Exception

class StockDetailViewModel : ViewModel() {

    private val stock = MutableLiveData<StockModel>()

    var observableStock: LiveData<StockModel>
        get() = stock
        set(value) {stock.value = value.value}


    fun getStock(userid: String, id: String) {
        try {
            StockManager.findById(userid,id, stock)
            Timber.i("Detail getstock() Success : ${
                stock.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Detail getstock() Error : $e.message")
        }
    }

    fun updateStock(userid: String, id: String,stock: StockModel) {
        try {
            StockManager.update(userid,id, stock)
            Timber.i("Detail update() Success : $stock")
        } catch (e: Exception) {
            Timber.i("Detail update() Error : $e.message")
        }
    }
}