package org.wit.inventorymanager.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BuildingModel(var id: Long = 0,
                         var name: String = "",
                         var address: String = "",
                         var image: String = "",
                         var phone: String = "",
                         var lat: Double = 0.0,
                         var lng: Double = 0.0,
                         var zoom: Float = 0f)
                         : Parcelable
    {
                        @Exclude
                        fun toMap(): Map<String, Any?> {
                            return mapOf(
                                "id" to id,
                                "name" to name,
                                "address" to address,
                                "image" to image,
                                "phone" to phone,
                                "lat" to lat,
                                "lng" to lng,
                                "zoom" to zoom
                            )
                        }

    }

@Parcelize
data class Location(var lat: Double = 0.0,
                    var lng: Double = 0.0,
                    var zoom: Float = 0f) : Parcelable