package org.wit.inventorymanager.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import org.wit.inventorymanager.models.StockModel
import org.wit.inventorymanager.models.StockStore
import timber.log.Timber
import java.util.ArrayList
import java.util.HashMap


object StockManager : StockStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference


    /**
     * We're using the Firebase database to get a list of all the stocks in the database, and then
     * we're setting the value of the MutableLiveData object to the list of stocks we got from the
     * database
     *
     * @param stockList MutableLiveData<List<StockModel>>
     */
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

    /**
     * It gets all the stocks from the database and returns them in a list.
     *
     * @param userid The user's id
     * @param stockList MutableLiveData<List<StockModel>>
     */
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
                    }
                    database.child("user-stock").child(userid)
                        .removeEventListener(this)

                    stockList.value = localList
                }
            })
    }

    /**
     * It searches for the stock in the database by building id.
     *
     * @param userid The user's id
     * @param buildingid The building id of the building you want to search for.
     * @param stockList MutableLiveData<List<StockModel>>
     */
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
                            if(stock?.branch == buildingid && stock.uid == userid) {
                                localList.add(stock)
                            }
                    }
                    database.child("user-stock").child(userid)
                        .removeEventListener(this)

                    stockList.value = localList
                }
            })
    }

    /**
     * It searches the database for a stock item that matches the search term and the building id
     *
     * @param userid The user's id
     * @param buildingid The building id of the user
     * @param term The search term
     * @param stockList MutableLiveData<List<StockModel>>
     */
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
                            val stock = it.getValue(StockModel::class.java)
                            if(stock?.name!!.contains(term) && stock.branch == buildingid) {
                                localList.add(stock)
                            }
                        }
                    database.child("user-stock").child(userid)
                        .removeEventListener(this)

                    stockList.value = localList
                }
            })
    }

    /**
     * We're getting the stock data from the database, and if we get it, we're setting the value of the
     * stock MutableLiveData object to the value we got from the database
     *
     * @param userid The user's id
     * @param stockid The id of the stock you want to get.
     * @param stock MutableLiveData<StockModel>
     */
    override fun findById(userid: String, stockid: String, stock: MutableLiveData<StockModel>) {

        database.child("user-stock").child(userid).child(stockid)
            .get().addOnSuccessListener {
                stock.value = it.getValue(StockModel::class.java)
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener{
                Timber.e("firebase Error getting data $it")
            }
    }

    /**
     * We create a new stock object in the database, and then we add it to the user's list of stocks
     *
     * @param firebaseUser MutableLiveData<FirebaseUser>
     * @param stock The StockModel object that you want to add to the database.
     * @return A HashMap of the stock and user-stock
     */
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

    /**
     * Delete the stock from the user's stock list and delete the stock from the stock list
     *
     * @param userid The user's id
     * @param stockid The stock's unique ID
     */
    override fun delete(userid: String, stockid: String) {

        val childDelete : MutableMap<String, Any?> = HashMap()
        childDelete["/stock/$stockid"] = null
        childDelete["/user-stock/$userid/$stockid"] = null

        database.updateChildren(childDelete)
    }

    /**
     * We create a map of the stock values, then we create a map of the stock values and the user-stock
     * values, then we update the database with the childUpdate map
     *
     * @param userid The userid of the user who owns the stock
     * @param stockid The unique ID of the stock
     * @param stock The stock to be updated
     */
    override fun update(userid: String, stockid: String, stock: StockModel) {

        val stockValues = stock.toMap()

        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["stock/$stockid"] = stockValues
        childUpdate["user-stock/$userid/$stockid"] = stockValues

        database.updateChildren(childUpdate)
    }


}

