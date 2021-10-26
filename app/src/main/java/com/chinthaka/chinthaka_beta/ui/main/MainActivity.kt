package com.chinthaka.chinthaka_beta.ui.main

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.RequestManager
import com.chinthaka.chinthaka_beta.AuthActivity
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.databinding.ActivityMainBinding
import com.chinthaka.chinthaka_beta.repositories.MetricRepository
import com.chinthaka.chinthaka_beta.ui.main.listeners.NavigationUpdateListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationUpdateListener {

    companion object {

        const val STORAGE_PERMISSION_CODE = 100
    }

    @Inject
    lateinit var glide: RequestManager

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navController: NavController
    public var storagePermissionChecked=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Chinthaka)


        //TODO - Need to understand the consequences of this
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val metricRepository = MetricRepository()
        metricRepository.recordDailyLogin()

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.appBarMain.toolbar)
        activityMainBinding.appBarMain.toolbar.background = AppCompatResources.getDrawable(this, R.color.white)
        activityMainBinding.appBarMain.toolbar.setTitleTextColor(
            AppCompatResources.getColorStateList(
                this,
                R.color.black
            )
        )
        checkPermissions();

        // This is being done as we are having a FragmentContainerView, not a direct Fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val drawerLayout: DrawerLayout = activityMainBinding.drawerLayout
        val navView: NavigationView = activityMainBinding.navigationView
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            activityMainBinding.appBarMain.toolbar,
            R.string.start,
            R.string.close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navController = navHostFragment.findNavController()

        val extras = intent.extras?.get(R.string.is_new_user.toString())

        if (extras == true) {
            navController.navigate(R.id.globalActionToSettingsFragment)
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment
            ), drawerLayout
        )

        activityMainBinding.appBarMain.toolbar.setupWithNavController(navController, drawerLayout)

        navView.getHeaderView(0).setOnClickListener {
            drawerLayout.close()
            navController.navigate(R.id.action_homeFragment_to_settingsFragment,
                Bundle().apply { putBoolean("is_navigated_from_drawer", true) })
        }

        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {
            FirebaseAuth.getInstance().signOut()
            Intent(this, AuthActivity::class.java).also {
                startActivity(it)
            }
            finish()
            true
        }

        navView.menu.findItem(R.id.settingsFragment).setOnMenuItemClickListener {
            drawerLayout.close()
            navController.navigate(R.id.action_homeFragment_to_settingsFragment,
                Bundle().apply { putBoolean("is_navigated_from_drawer", true) })
            true
        }

        // Accessing Drawer Header Views
//        val ivProfileImage = navView.getHeaderView(0).findViewById<ImageView>(R.id.ivProfileImage)
//        val tvProfileName = navView.getHeaderView(0).findViewById<TextView>(R.id.tvProfileName)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        activityMainBinding.appBarMain.bottomNavigationView.apply {
            background = null
            setupWithNavController(navHostFragment.findNavController())
        }

        handleDeepLinkingIntent(intent)
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

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }

    override fun onUserDataChanged(user: User) {
        if(user!=null) {
            updateNavigationUserDetails(user)
        }
    }

    private fun updateNavigationUserDetails(user: User){

        val ivDrawerImage : ImageView = activityMainBinding.navigationView.findViewById(R.id.ivProfileImage)

        glide.load(user.profilePictureUrl).into(ivDrawerImage)

        val userName : TextView = activityMainBinding.navigationView.findViewById(R.id.tvProfileName)
        userName.text = user.userName

    }

    private fun handleDeepLinkingIntent(intent: Intent?) {
        val appLinkAction: String? = intent?.action
        val appLinkData: Uri? = intent?.data
        redirectToViewPostFragmentForDeepLink(appLinkAction, appLinkData)
    }

    private fun redirectToViewPostFragmentForDeepLink(appLinkAction: String?, appLinkData: Uri?) {
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            val postId = appLinkData.getQueryParameter("id")
            if (postId.isNullOrBlank().not()) {
                Log.d("post-id","" + postId)
                navController.navigate(R.id.globalActionToViewPostFragment,Bundle().apply { putString("postId",postId) })
            } else {
                Log.d("post-id","Post Id is null")
            }
        }
    }
    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }


    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                storagePermissionChecked=true
                Toast.makeText(this@MainActivity, "Storage Permission Granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this@MainActivity, "Storage Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun checkPermissions(){
        checkPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            STORAGE_PERMISSION_CODE)
       // storage?.isVisible=false;
        /*// Defining storage Button
        val storage: Button? = findViewById(R.id.storage)
        // Set Buttons on Click Listeners , check for storage permission
        storage?.setOnClickListener {
            */

        }


}