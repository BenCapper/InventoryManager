package org.wit.inventorymanager.activities


import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.firebase.auth.FirebaseUser
import org.wit.inventorymanager.databinding.HomeBinding
import org.wit.inventorymanager.R
import org.wit.inventorymanager.databinding.NavHeaderBinding
import org.wit.inventorymanager.helpers.checkLocationPermissions
import org.wit.inventorymanager.helpers.isPermissionGranted
import org.wit.inventorymanager.ui.auth.LoggedInViewModel
import org.wit.inventorymanager.ui.auth.Login
import org.wit.inventorymanager.ui.maps.MapsViewModel
import timber.log.Timber


class Home : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var homeBinding : HomeBinding
    private val mapsViewModel : MapsViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var loggedInViewModel : LoggedInViewModel
    private lateinit var navHeaderBinding : NavHeaderBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeBinding = HomeBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)
        drawerLayout = homeBinding.drawerLayout
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        val navView = homeBinding.navView
        navView.setupWithNavController(navController)
        if(checkLocationPermissions(this)) {
            mapsViewModel.updateCurrentLocation()
        }


        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.buildingListFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)


    }

    public override fun onStart() {
        super.onStart()
        loggedInViewModel = ViewModelProvider(this).get(LoggedInViewModel::class.java)
        loggedInViewModel.liveFirebaseUser.observe(this, Observer { firebaseUser ->
            if (firebaseUser != null)
                updateNavHeader(loggedInViewModel.liveFirebaseUser.value!!)
        })

        loggedInViewModel.loggedOut.observe(this, Observer { loggedout ->
            if (loggedout) {
                startActivity(Intent(this, Login::class.java))
            }
        })

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @SuppressLint("MissingPermission")
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

    private fun updateNavHeader(currentUser: FirebaseUser) {
        var headerView = homeBinding.navView.getHeaderView(0)
        navHeaderBinding = NavHeaderBinding.bind(headerView)
        navHeaderBinding.navHeaderEmail.text = currentUser.email
    }

    fun signOut(item: MenuItem) {
        loggedInViewModel.logOut()
        //Launch Login activity and clear the back stack to stop navigating back to the Home activity
        val intent = Intent(this, Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
