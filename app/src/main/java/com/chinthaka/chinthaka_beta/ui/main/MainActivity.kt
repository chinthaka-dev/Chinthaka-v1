package com.chinthaka.chinthaka_beta.ui.main

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.bumptech.glide.RequestManager
import com.chinthaka.chinthaka_beta.AuthActivity
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.databinding.ActivityMainBinding
import com.chinthaka.chinthaka_beta.ui.main.fragments.InterestsFragmentDirections
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


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
        activityMainBinding.appBarMain.toolbar.background = AppCompatResources.getDrawable(this, R.color.black)
        activityMainBinding.appBarMain.toolbar.setTitleTextColor(AppCompatResources.getColorStateList(this, R.color.white))

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

        val extras = intent.extras?.get(R.string.is_new_user.toString())

        if(extras == true)
            navController.navigate(R.id.globalActionToInterestsFragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.profileFragment, R.id.settingsFragment, R.id.bookmarksFragment, R.id.othersProfileFragment
            ), drawerLayout
        )

        activityMainBinding.appBarMain.toolbar.setupWithNavController(navController, drawerLayout)

        navView.getHeaderView(0).setOnClickListener {
            drawerLayout.close()
            navController.navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        // Accessing Drawer Header Views
//        val ivProfileImage = navView.getHeaderView(0).findViewById<ImageView>(R.id.ivProfileImage)
//        val tvProfileName = navView.getHeaderView(0).findViewById<TextView>(R.id.tvProfileName)



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

}