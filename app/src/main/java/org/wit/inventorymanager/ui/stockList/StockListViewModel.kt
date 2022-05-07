package org.wit.inventorymanager.ui.stockList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.wit.inventorymanager.models.StockManager
import org.wit.inventorymanager.models.StockModel
import timber.log.Timber
import java.lang.Exception

class StockListViewModel : ViewModel() {

    private val stockList =
        MutableLiveData<List<StockModel>>()

    val observableStockList: LiveData<List<StockModel>>
        get() = stockList

    private val stock = MutableLiveData<StockModel>()

    var observableStock: LiveData<StockModel>
        get() = stock
        set(value) {stock.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    var readOnly = MutableLiveData(false)

    init { load() }

    fun load() {
        try {
            readOnly.value = false
            StockManager.findAll(liveFirebaseUser.value?.uid!!,stockList)
            Timber.i("Load Success : ${stockList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun loadAll(userid: String,buildingid: String) {
        try {
            readOnly.value = true
            StockManager.searchByBuild(userid,buildingid,stockList)
            Timber.i("stocklist LoadAll Success : ${stockList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("stocklist LoadAll Error : $e.message")
        }
    }

    fun search(userid: String,buildingid: String, term: String) {
        try {
            StockManager.search(userid,buildingid,term,stockList)
            Timber.i("Stock Search Success")
        }
        catch (e: Exception) {
            Timber.i("Stock Search Error : $e.message")
        }
    }

    fun searchByBuild(userid: String,buildingid: String){
        try {
            StockManager.searchByBuild(userid,buildingid,stockList)
            Timber.i("Stock Search Success")
        }
        catch (e: Exception) {
            Timber.i("Stock Search Error : $e.message")
        }
    }

    fun delete(userid: String, id: String) {
        try {
            StockManager.delete(userid,id)
            Timber.i("Stock Delete Success")
        }
        catch (e: Exception) {
            Timber.i("Stock Delete Error : $e.message")
        }
    }

    fun update(userid: String, id: String, stock: StockModel) {
        try {
            StockManager.update(userid,id,stock)
            Timber.i("Detail update() Success : $stock")
        } catch (e: Exception) {
            Timber.i("Detail update() Error : $e.message")
        }
    }
}