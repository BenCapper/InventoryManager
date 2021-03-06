package org.wit.inventorymanager.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StockModel(var id: String = "",
                      var uid: String = "",
                      var name: String = "",
                      var branch: String = "",
                      var weight: String = "",
                      var unit: String = "",
                      var price: Double = 0.00,
                      var inStock: Int = 0,
                      var max: Int = 0)
    : Parcelable
{
        @Exclude
        fun toMap(): Map<String, Any?> {
            return mapOf(
                "id" to id,
                "uid" to uid,
                "name" to name,
                "branch" to branch,
                "weight" to weight,
                "unit" to unit,
                "price" to price,
                "inStock" to inStock,
                "max" to max,

            )
        }

    }