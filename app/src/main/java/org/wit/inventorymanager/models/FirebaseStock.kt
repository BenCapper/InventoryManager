package org.wit.inventorymanager.models

import com.google.firebase.database.FirebaseDatabase
import java.util.*

class FirebaseStock : StockStore {

    var stock = mutableListOf<StockModel>()
    val db = FirebaseDatabase.getInstance("https://invmanage-4bcbd-default-rtdb.firebaseio.com")
        .getReference("Building")


    override fun findAll(): MutableList<StockModel> {
        return stock
    }


    fun generateRandomStockId(): Long {
        return Random().nextLong()
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
        return stock.filter { item -> item.branch != id }
    }
}


