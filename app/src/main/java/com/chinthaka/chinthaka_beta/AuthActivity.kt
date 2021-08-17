package com.chinthaka.chinthaka_beta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.chinthaka.chinthaka_beta.databinding.ActivityAuthBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.auth.AuthViewModel
import com.chinthaka.chinthaka_beta.ui.main.MainActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class AuthActivity : AppCompatActivity() {

    private lateinit var activityAuthBinding: ActivityAuthBinding

    private lateinit var viewModel: AuthViewModel

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityAuthBinding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(activityAuthBinding.root)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        subscribeToObservers()

        // Stay Loggedin Functionality
        if (FirebaseAuth.getInstance().currentUser != null) {
            // This means that User is still logged in
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        } else createSignInIntent()
    }

    // [START auth_fui_result]
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            if(response!!.isNewUser){
                viewModel.addNewUserViaSocialLogin(FirebaseAuth.getInstance().currentUser)
                Intent(this, ChooseInterestsActivity::class.java).also {
                    startActivity(it)
                    finish()
                }
            } else {
                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                    finish()
                }
            }

        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
        }
    }

    private fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
        // [END auth_fui_create_intent]
    }

    private fun themeAndLogo() {
        val providers = emptyList<AuthUI.IdpConfig>()

        // [START auth_fui_theme_logo]
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.ic_dashboard_black_24dp) // Set logo drawable
//            .setTheme(R.style.MySuperAppTheme) // Set theme
            .build()
        signInLauncher.launch(signInIntent)
        // [END auth_fui_theme_logo]
    }

    private fun subscribeToObservers() {
        viewModel.addNewUserStatus.observe(this, EventObserver(
            onError = {
                activityAuthBinding.addNewUserProgressBar.isVisible = false
//                snackbar(it, rootView)
            },
            onLoading = {
                activityAuthBinding.addNewUserProgressBar.isVisible = true
            }
        ) {
            activityAuthBinding.addNewUserProgressBar.isVisible = false
//            snackbar(getString(R.string.success_registration))
        })
    }

}