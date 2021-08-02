package com.chinthaka.chinthaka_beta.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.chinthaka.chinthaka_beta.AuthActivity
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        // This is being done as we are having a FragmentContainerView, not a direct Fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
                as NavHostFragment

//        activityMainBinding.bottomNavigationView.apply {
//            background = null
//            menu.getItem(2).isVisible = false // Hiding the Placeholder Item
//            setupWithNavController(navHostFragment.findNavController())
//        }

//        activityMainBinding.fabNewPost.setOnClickListener{
//            navHostFragment.findNavController().navigate(
//                R.id.globalActionToCreatePostFragment
//            )
//        }

        activityMainBinding.bottomNavigationView.setOnItemSelectedListener{

            when(it.itemId){
                R.id.homeFragment -> {
                    navHostFragment.findNavController().navigate(R.id.globalActionToHomeFragment)
                }
                R.id.createPostFragment -> {
                    navHostFragment.findNavController().navigate(R.id.globalActionToCreatePostFragment)
                }
                R.id.bookmarksFragment -> {
                    navHostFragment.findNavController().navigate(R.id.globalActionToBookmarksFragment)
                }
            }

            return@setOnItemSelectedListener true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miLogout -> {
                FirebaseAuth.getInstance().signOut()
                Intent(this, AuthActivity::class.java).also {
                    startActivity(it)
                }
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}