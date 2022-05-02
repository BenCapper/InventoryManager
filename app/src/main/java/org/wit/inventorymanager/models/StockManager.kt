package org.wit.inventorymanager.models

import com.google.firebase.database.FirebaseDatabase


object StockManager : StockStore {

    private var stock = mutableListOf<StockModel>()
    private val db = FirebaseDatabase.getInstance("https://invmanage-4bcbd-default-rtdb.firebaseio.com")
        .getReference("Stock")


    override fun findAll(): MutableList<StockModel> {
        return stock
    }


    override fun create(stock: StockModel) {
        db.child(stock.id.toString()).setValue(stock)
    }


    override fun update(stock: StockModel) {
        db.child(stock.id.toString()).setValue(stock)

    }

    override fun delete(stock: StockModel) {
        db.child(stock.id.toString()).removeValue()

    }


    override fun filterStock(id: Long): List<StockModel> {
        return stock.filter { item -> item.branch.equals(id)}
    }
}


