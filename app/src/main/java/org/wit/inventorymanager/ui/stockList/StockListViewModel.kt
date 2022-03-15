package org.wit.inventorymanager.ui.stockList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wit.inventorymanager.models.StockManager
import org.wit.inventorymanager.models.StockModel

class StockListViewModel : ViewModel() {

    private val stockList = MutableLiveData<List<StockModel>>()

    val observableStockList: LiveData<List<StockModel>>
        get() = stockList

    init {
        load()
    }

    private fun load() {
        stockList.value = StockManager.findAll()
    }
}