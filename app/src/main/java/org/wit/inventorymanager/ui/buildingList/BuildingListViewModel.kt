package org.wit.inventorymanager.ui.buildingList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.wit.inventorymanager.firebase.BuildingManager
import org.wit.inventorymanager.models.BuildingModel
import timber.log.Timber
import java.lang.Exception

class BuildingListViewModel : ViewModel() {

    private val buildingList =
        MutableLiveData<List<BuildingModel>>()

    val observableBuildingList: LiveData<List<BuildingModel>>
        get() = buildingList

    private val build = MutableLiveData<BuildingModel>()

    var observableBuild: LiveData<BuildingModel>
        get() = build
        set(value) {build.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    var readOnly = MutableLiveData(false)

    init { load() }

    /**
     * `BuildingManager.findAll(liveFirebaseUser.value?.uid!!, buildingList)`
     *
     * This function is called from the `BuildingManager` class, which is a singleton class that
     * handles all the Firebase calls. The `findAll` function takes two parameters: the user's id and
     * the `MutableLiveData` object that will hold the list of buildings
     */
    fun load() {
        try {
            readOnly.value = false
            BuildingManager.findAll(liveFirebaseUser.value?.uid!!,
                buildingList)
            Timber.i("Load Success : ${buildingList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    /**
     * `loadAll()` is a function that loads all the buildings from the database and puts them in the
     * `buildingList` variable
     */
    fun loadAll() {
        try {
            readOnly.value = true
            BuildingManager.findAll(buildingList)
            Timber.i("LoadAll Success : ${buildingList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("LoadAll Error : $e.message")
        }
    }

    /**
     * The function takes in a userid and a search term, and then calls the search function in the
     * BuildingManager class, which returns a list of buildings that match the search term
     *
     * @param userid The user's id
     * @param term The search term
     */
    fun search(userid: String, term: String) {
        try {
            BuildingManager.search(userid,term,buildingList)
            Timber.i("Building Search Success")
        }
        catch (e: Exception) {
            Timber.i("Building Search Error : $e.message")
        }
    }

    /**
     * It deletes a building from the database.
     *
     * @param userid The user's id
     * @param id The id of the building you want to delete.
     */
    fun delete(userid: String, id: String) {
        try {
            BuildingManager.delete(userid,id)
            Timber.i("Building Delete Success")
        }
        catch (e: Exception) {
            Timber.i("Building Delete Error : $e.message")
        }
    }

    /**
     * It updates the building with the given id.
     *
     * @param userid The user's ID.
     * @param id The id of the building you want to update.
     * @param build BuildingModel
     */
    fun update(userid: String, id: String, build:BuildingModel) {
        try {
            BuildingManager.update(userid,id, build)
            Timber.i("Detail update() Success : $build")
        } catch (e: Exception) {
            Timber.i("Detail update() Error : $e.message")
        }
    }
}