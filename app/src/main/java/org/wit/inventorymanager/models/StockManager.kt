package org.wit.inventorymanager.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import timber.log.Timber
import java.util.ArrayList
import java.util.HashMap


object StockManager : StockStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun findAll(stockList: MutableLiveData<List<StockModel>>) {
        database.child("stock")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase stock error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<StockModel>()
                    val children = snapshot.children
                    children.forEach {
                        val stock = it.getValue(StockModel::class.java)
                        localList.add(stock!!)
                    }
                    database.child("stock")
                        .removeEventListener(this)

                    stockList.value = localList
                }
            })
    }

    override fun findAll(
        userid: String,
        stockList: MutableLiveData<List<StockModel>>
    ) {

        database.child("user-stock").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase stock error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<StockModel>()
                    val children = snapshot.children
                    children.forEach {
                        val stock= it.getValue(StockModel::class.java)
                            localList.add(stock!!)
                            Timber.i("TTTTTTTTTTTTTIMBER::::    ${stock}")
                    }
                    database.child("user-stock").child(userid)
                        .removeEventListener(this)

                    stockList.value = localList
                }
            })
    }

    override fun searchByBuild(userid: String,buildingid: String, stockList: MutableLiveData<List<StockModel>>) {

        database.child("user-stock").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase stock error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<StockModel>()
                    val children = snapshot.children
                    children.forEach {
                            val stock = it.getValue(StockModel::class.java)
                            if(stock?.branch == buildingid && stock?.uid == userid) {
                                localList.add(stock!!)
                                Timber.i("TTTTTTTTTTTTTIMBER::::    ${stock}")
                            }
                    }
                    database.child("user-stock").child(userid)
                        .removeEventListener(this)

                    stockList.value = localList
                }
            })
    }

    override fun search(userid: String,buildingid: String, term: String, stockList: MutableLiveData<List<StockModel>>) {

        database.child("user-stock").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase stock error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<StockModel>()
                    val children = snapshot.children
                    children.forEach {
                        if (it.getValue(StockModel::class.java)?.name!!.contains(term) ) {
                            val stock = it.getValue(StockModel::class.java)
                            if(stock?.branch == buildingid && stock?.uid == userid) {
                                localList.add(stock!!)
                            }
                            localList.add(stock!!)
                        }
                    }
                    database.child("user-stock").child(userid)
                        .removeEventListener(this)

                    stockList.value = localList
                }
            })
    }

    override fun findById(userid: String, stockid: String, stock: MutableLiveData<StockModel>) {

        database.child("user-stock").child(userid)
            .get().addOnSuccessListener {
                stock.value = it.getValue(StockModel::class.java)
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener{
                Timber.e("firebase Error getting data $it")
            }
    }

    override fun create(firebaseUser: MutableLiveData<FirebaseUser>, stock: StockModel) {
        Timber.i("Firebase DB Reference : $database")

        val uid = firebaseUser.value!!.uid
        val key = database.child("stock").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        stock.id = key
        val stockValues = stock.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/stock/$key"] = stockValues
        childAdd["/user-stock/$uid/$key"] = stockValues

        database.updateChildren(childAdd)
    }

    override fun delete(userid: String, stockid: String) {

        val childDelete : MutableMap<String, Any?> = HashMap()
        childDelete["/stock/$stockid"] = null
        childDelete["/user-stock/$userid/$stockid"] = null

        database.updateChildren(childDelete)
    }

    override fun update(userid: String, stockid: String, stock: StockModel) {

        val stockValues = stock.toMap()

        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["stock/$stockid"] = stockValues
        childUpdate["user-stock/$userid/$stockid"] = stockValues

        database.updateChildren(childUpdate)
    }


    fun updateImageRef(userid: String,imageUri: String) {

        val userStock = database.child("user-stock").child(userid)
        val allStock = database.child("stock")

        userStock.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        //Update Users imageUri
                        it.ref.child("image").setValue(imageUri)
                        //Update all donations that match 'it'
                        val stock = it.getValue(StockModel::class.java)
                        allStock.child(stock!!.uid!!)
                            .child("image").setValue(imageUri)
                    }
                }
            })
    }

}

