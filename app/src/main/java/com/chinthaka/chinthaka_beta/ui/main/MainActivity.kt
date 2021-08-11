package com.chinthaka.chinthaka_beta.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.chinthaka.chinthaka_beta.AuthActivity
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: AppCompatActivity(){

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setSupportActionBar(activityMainBinding.appBarMain.toolbar)

        // This is being done as we are having a FragmentContainerView, not a direct Fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
                as NavHostFragment
        val drawerLayout: DrawerLayout = activityMainBinding.drawerLayout
        val navView: NavigationView = activityMainBinding.navigationView
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, activityMainBinding.appBarMain.toolbar, R.string.start, R.string.close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navController = navHostFragment.findNavController()

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.profileFragment, R.id.settingsFragment, R.id.interestsFragment, R.id.bookmarksFragment, R.id.othersProfileFragment
            ), drawerLayout
        )

        activityMainBinding.appBarMain.toolbar.setupWithNavController(navController, drawerLayout)

        navView.setNavigationItemSelectedListener{
            when(it.itemId){
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    Intent(this, AuthActivity::class.java).also {
                        startActivity(it)
                    }
                    finish()
                }
            }
            true
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        activityMainBinding.appBarMain.bottomNavigationView.apply {
            background = null
            setupWithNavController(navHostFragment.findNavController())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true
        }
        return item.onNavDestinationSelected(navController = navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.navHostFragment)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun setBottomNavigationVisibility(visibility: Int) {
        // get the reference of the bottomNavigationView and set the visibility.
        activityMainBinding.appBarMain.bottomNavigationView.visibility = visibility
    }

}