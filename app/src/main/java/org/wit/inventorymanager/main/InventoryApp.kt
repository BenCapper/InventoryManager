package org.wit.inventorymanager.main

import android.app.Application


import org.wit.inventorymanager.models.BuildingStore
import org.wit.inventorymanager.firebase.BuildingManager
import org.wit.inventorymanager.firebase.StockManager
import org.wit.inventorymanager.models.StockStore
import timber.log.Timber

class InventoryApp : Application() {

    lateinit var builds: BuildingStore
    lateinit var stocks: StockStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        builds = BuildingManager
        stocks = StockManager
        Timber.i("Inventory Manager Started")
    }
}