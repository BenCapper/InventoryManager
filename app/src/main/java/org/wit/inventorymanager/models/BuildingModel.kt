package org.wit.inventorymanager.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BuildingModel(var id: String = "",
                         var uid: String = "",
                         var name: String = "",
                         var town: String = "",
                         var county: String = "",
                         var phone: String = "",
                         var staff: Int = 0,
                         var profilepic: String? = "",
                         var hiring: Boolean? = null,
                         var faved: Boolean = false,
                         var lat: String = "",
                         var lng: String = "", )
                         : Parcelable
    {
                        @Exclude
                        fun toMap(): Map<String, Any?> {
                            return mapOf(
                                "id" to id,
                                "uid" to uid,
                                "name" to name,
                                "town" to town,
                                "county" to county,
                                "phone" to phone,
                                "staff" to staff,
                                "profilepic" to profilepic,
                                "hiring" to hiring,
                                "faved" to faved,
                                "lat" to lat,
                                "lng" to lng,
                            )
                        }

    }
