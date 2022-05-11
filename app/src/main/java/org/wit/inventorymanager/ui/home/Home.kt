package org.wit.inventorymanager.ui.home


import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.firebase.auth.FirebaseUser
import org.wit.inventorymanager.databinding.HomeBinding
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.NavHeaderBinding
import org.wit.inventorymanager.firebase.FirebaseImageManager
import org.wit.inventorymanager.helpers.checkLocationPermissions
import org.wit.inventorymanager.helpers.isPermissionGranted
import org.wit.inventorymanager.helpers.readImageUri
import org.wit.inventorymanager.helpers.showImagePicker
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.auth.Login
import org.wit.inventorymanager.ui.maps.MapsViewModel
import timber.log.Timber


class Home : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var homeBinding : HomeBinding
    private lateinit var navHeaderBinding : NavHeaderBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var loggedInViewModel : LoggedInViewModel
    private lateinit var headerView : View
    private lateinit var intentLauncher : ActivityResultLauncher<Intent>
    private val mapsViewModel : MapsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeBinding = HomeBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)
        drawerLayout = homeBinding.drawerLayout
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager.
        findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.buildingListFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val navView = homeBinding.navView
        navView.setupWithNavController(navController)

        initNavHeader()

        if(checkLocationPermissions(this)) {
            mapsViewModel.updateCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    /**
     * If the user grants the permission, update the current location, otherwise use a default location
     *
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[],
     * int)
     * @param permissions The permissions that were requested.
     * @param grantResults An array of the results of each permission
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isPermissionGranted(requestCode, grantResults))
            mapsViewModel.updateCurrentLocation()
        else {
            // permissions denied, so use a default location
            mapsViewModel.currentLocation.value = Location("Default").apply {
                latitude = 52.245696
                longitude = -7.139102
            }
        }
        Timber.i("LOC : %s", mapsViewModel.currentLocation.value)
    }

    public override fun onStart() {
        super.onStart()
        /* This is the code that is used to observe the logged in user and update the nav header with
        the user's details. */
        loggedInViewModel = ViewModelProvider(this)[LoggedInViewModel::class.java]
        loggedInViewModel.liveFirebaseUser.observe(this) { firebaseUser ->
            if (firebaseUser != null) {
                updateNavHeader(firebaseUser)
            }
        }

        loggedInViewModel.loggedOut.observe(this) { loggedOut ->
            if (loggedOut) {
                startActivity(Intent(this, Login::class.java))
            }
        }

        registerImagePickerCallback()
    }

    /**
     * We get the header view from the navigation view, bind it to a NavHeaderBinding object, and set
     * an onClickListener on the imageView
     */
    private fun initNavHeader() {
        Timber.i("Init Nav Header")
        headerView = homeBinding.navView.getHeaderView(0)
        navHeaderBinding = NavHeaderBinding.bind(headerView)
        navHeaderBinding.imageView.setOnClickListener {
            showImagePicker(intentLauncher)
        }
    }

    /**
     * If the user has an image stored in Firebase, load it. If not, load the default image
     *
     * @param currentUser FirebaseUser - the current user
     */
    private fun updateNavHeader(currentUser: FirebaseUser) {
        FirebaseImageManager.checkStorageForExistingProfilePic(currentUser.uid)
        FirebaseImageManager.imageUri.observe(this) { result ->
            if (result == Uri.EMPTY) {
                Timber.i("NO Existing imageUri")
                if (currentUser.photoUrl != null) {
                    //if you're a google user
                    FirebaseImageManager.updateUserImage(
                        currentUser.uid,
                        currentUser.photoUrl,
                        navHeaderBinding.imageView,
                        true
                    )
                } else {
                    Timber.i("Loading Existing Default imageUri")
                    FirebaseImageManager.updateDefaultImage(
                        currentUser.uid,
                        R.drawable.ic_stock,
                        navHeaderBinding.imageView
                    )
                }
            } else // load existing image from firebase
            {
                Timber.i("Loading Existing imageUri")
                FirebaseImageManager.updateUserImage(
                    currentUser.uid,
                    FirebaseImageManager.imageUri.value,
                    navHeaderBinding.imageView, false
                )
            }
        }
        navHeaderBinding.navHeaderEmail.text = currentUser.email
        if(currentUser.displayName != null)
            navHeaderBinding.navHeaderEmail.text = currentUser.displayName
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * It logs the user out and redirects them to the login page.
     *
     * @param item MenuItem - The menu item that was clicked.
     */
    fun signOut(item: MenuItem) {
        loggedInViewModel.logOut()
        val intent = Intent(this, Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    /**
     * It navigates to the Favourite Maps Fragment
     *
     * @param item MenuItem - this is the item that was clicked on.
     */
    fun faveLocation(item: MenuItem){
        findNavController(R.id.nav_host_fragment).navigate(R.id.faveMapsFragment)
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawers()
    }

    /**
     * It registers a callback for the image picker.
     */
    private fun registerImagePickerCallback() {
        intentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when(result.resultCode){
                    RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("registerPickerCallback() ${readImageUri(result.resultCode, result.data).toString()}")
                            FirebaseImageManager
                                .updateUserImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    navHeaderBinding.imageView,
                                    true)
                        } // end of if
                    }
                    RESULT_CANCELED -> { } else -> { }
                }
            }
    }
}