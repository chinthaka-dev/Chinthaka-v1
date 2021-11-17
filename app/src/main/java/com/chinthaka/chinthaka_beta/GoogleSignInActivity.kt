package com.chinthaka.chinthaka_beta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.chinthaka.chinthaka_beta.databinding.ActivityGoogleAuthBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.repositories.LogRepository
import com.chinthaka.chinthaka_beta.ui.auth.AuthViewModel
import com.chinthaka.chinthaka_beta.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleSignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var viewModel: AuthViewModel

    private val logRepository = LogRepository()

    private lateinit var binding: ActivityGoogleAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logRepository.recordLog("Inside AuthActivity")
        binding = ActivityGoogleAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        subscribeToObservers()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        val extras = intent.extras?.get(R.string.sign_out.toString())
        if (extras == true) {
            googleSignInClient.signOut()
        } else if (isSignedIn()) {
            val appLinkAction: String? = intent?.action
            val appLinkData: Uri? = intent?.data
            logRepository.recordLog("Authentication Successful")
            // This means that User is still logged in
            Intent(appLinkAction,appLinkData,this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
        binding.signInButton.setOnClickListener {
            signIn()
        }

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    val isNewUser = task.result.additionalUserInfo!!.isNewUser
                    if(isNewUser) {
                        viewModel.addNewUserViaSocialLogin(user)
                    }
                    updateUI(user)
                    Intent(this, MainActivity::class.java).also {
                        it.putExtra(R.string.is_new_user.toString(), isNewUser)
                        startActivity(it)
                        finish()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun updateUI(user: FirebaseUser?) {

    }

    private fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(this) != null
    }

    private fun subscribeToObservers() {
        viewModel.addNewUserStatus.observe(this, EventObserver(
            onError = {
                binding.addNewUserProgressBar.isVisible = false
            },
            onLoading = {
                binding.addNewUserProgressBar.isVisible = true
            }
        ) {
            binding.addNewUserProgressBar.isVisible = false
        })
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}