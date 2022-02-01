package org.wit.inventorymanager.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import org.wit.inventorymanager.databinding.HomeBinding
import org.wit.inventorymanager.R

class Home : AppCompatActivity() {
    private val imageView: ImageView by lazy {
        findViewById(R.id.buildingImage)
    }
    private val selectPictureLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageView.setImageURI(it)
    }
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var homeBinding : HomeBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

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

        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.buildingFragment, R.id.buildingListFragment,), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        findViewById<Button>(R.id.chooseImage).setOnClickListener{
            selectPictureLauncher.launch("image/*")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}