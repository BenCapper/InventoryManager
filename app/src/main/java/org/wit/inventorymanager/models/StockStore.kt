package org.wit.inventorymanager.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface StockStore {
    fun findAll(stockList: MutableLiveData<List<StockModel>>)
    fun findAll(userid:String, stockList: MutableLiveData<List<StockModel>>)
    fun findById(userid:String, stockid: String, stock: MutableLiveData<StockModel>)
    fun create(firebaseUser: MutableLiveData<FirebaseUser>, stock: StockModel)
    fun delete(userid:String,  stockid: String)
    fun update(userid:String, stockid: String , stock: StockModel)
    fun search(userid: String, buildingid: String,term: String, stockList: MutableLiveData<List<StockModel>>)
    fun searchByBuild(userid: String,buildingid: String, stockList: MutableLiveData<List<StockModel>>)
}