package com.chinthaka.chinthaka_beta

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Int = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        setupDelayTime()
    }

    private fun setupDelayTime() {
        Handler().postDelayed({ nextActivity() }, SPLASH_TIME_OUT.toLong())
    }

    private fun nextActivity() {
        startActivity(newIntent(this))
        finish()
    }

    fun newIntent(context: Context): Intent {
        return Intent(context, GoogleSignInActivity::class.java)
    }

}