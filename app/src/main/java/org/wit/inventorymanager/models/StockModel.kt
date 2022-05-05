package org.wit.inventorymanager.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StockModel(var id: String = "",
                      var name: String = "",
                      var branch: String = "",
                      var weight: String = "",
                      var price: Double = 0.00,
                      var inStock: Long = 0,
                      var image: String = "",) : Parcelable