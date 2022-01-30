package org.wit.inventorymanager.main

import android.app.Application


import org.wit.inventorymanager.models.BuildingStore
import org.wit.inventorymanager.models.FirebaseBuildings
import org.wit.inventorymanager.models.FirebaseStock
import org.wit.inventorymanager.models.StockStore
import timber.log.Timber

class InventoryApp : Application() {

    lateinit var builds: BuildingStore
    lateinit var stocks: StockStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        builds = FirebaseBuildings()
        stocks = FirebaseStock()
        Timber.i("Inventory Manager Started")
    }
}