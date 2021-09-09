package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.data.entities.ProfileUpdate
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.databinding.FragmentSettingsBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.listeners.NavigationUpdateListener
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.SettingsViewModel
import com.chinthaka.chinthaka_beta.ui.slideUpViews
import com.chinthaka.chinthaka_beta.ui.snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.common.StringUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var fragmentSettingsBinding: FragmentSettingsBinding

    private val viewModel: SettingsViewModel by viewModels()

    private var curImageUri: Uri? = null

    private val args: SettingsFragmentArgs by navArgs()

    private lateinit var cropContent: ActivityResultLauncher<Any?>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSettingsBinding = FragmentSettingsBinding.bind(view)

        val isNavigatedFromDrawer: Boolean = args.isNavigatedFromDrawer

        subscribeToObservers()
        val userId = FirebaseAuth.getInstance().uid!!
        viewModel.getUser(userId)
        fragmentSettingsBinding.btnUpdateProfile.isEnabled = false

        fragmentSettingsBinding.etUsername.addTextChangedListener{
            if(fragmentSettingsBinding.etUsername.text!!.isNotEmpty() &&
                    fragmentSettingsBinding.etDescription.text!!.isNotEmpty()){
                fragmentSettingsBinding.btnUpdateProfile.isEnabled = true
            }
        }

        fragmentSettingsBinding.etDescription.addTextChangedListener{
            if(fragmentSettingsBinding.etUsername.text!!.isNotEmpty() &&
                fragmentSettingsBinding.etDescription.text!!.isNotEmpty()){
                fragmentSettingsBinding.btnUpdateProfile.isEnabled = true
            }
        }

        fragmentSettingsBinding.ivProfileImage.setOnClickListener {
            cropContent.launch(null)
        }

        fragmentSettingsBinding.btnUpdateProfile.setOnClickListener {
            val userName = fragmentSettingsBinding.etUsername.text.toString()
            val description = fragmentSettingsBinding.etDescription.text.toString()
            val profileUpdate = ProfileUpdate(userId, userName, description, curImageUri)
            viewModel.updateProfile(profileUpdate)
            if(!isNavigatedFromDrawer){
                findNavController().navigate(R.id.action_settingsFragment_to_InterestsFragment)
            }
        }

        slideUpViews(
            requireContext(),
            fragmentSettingsBinding.ivProfileImage,
            fragmentSettingsBinding.etUsername,
            fragmentSettingsBinding.etDescription,
            fragmentSettingsBinding.btnUpdateProfile
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropContent = registerForActivityResult(cropActivityResultContract) { uri ->
            uri?.let {
                viewModel.setCurImageUri(it)
                fragmentSettingsBinding.btnUpdateProfile.isEnabled = true
            }
        }
    }

    private val cropActivityResultContract = object : ActivityResultContract<Any?, Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {
            // This fun returns the Intent which will launch the CROP Activity

            return CropImage.activity()
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(requireContext())
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            // This fun returns Output Type (URI)
            return CropImage.getActivityResult(intent)?.uriContent
        }
    }  // Getting result from the CROP activity

    private fun subscribeToObservers(){
        viewModel.getUserStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                fragmentSettingsBinding.settingsProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {fragmentSettingsBinding.settingsProgressBar.isVisible = true }
        ) { user ->
            fragmentSettingsBinding.settingsProgressBar.isVisible = false
            glide.load(user.profilePictureUrl).into(fragmentSettingsBinding.ivProfileImage)
            fragmentSettingsBinding.etUsername.setText(user.userName)
            fragmentSettingsBinding.etDescription.setText(user.description)
            (requireActivity() as NavigationUpdateListener).onUserDataChanged(user)
        })

        viewModel.curImageUri.observe(viewLifecycleOwner) { uri ->
            curImageUri = uri
            glide.load(uri).into(fragmentSettingsBinding.ivProfileImage)
        }

        viewModel.updateProfileStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                fragmentSettingsBinding.settingsProgressBar.isVisible = false
                snackbar(it)
                fragmentSettingsBinding.btnUpdateProfile.isEnabled = true
            },
            onLoading = {
                fragmentSettingsBinding.settingsProgressBar.isVisible = true
                fragmentSettingsBinding.btnUpdateProfile.isEnabled = false
            }
        ) {
            fragmentSettingsBinding.settingsProgressBar.isVisible = false
            fragmentSettingsBinding.btnUpdateProfile.isEnabled = false
            snackbar(requireContext().getString(R.string.profile_updated))
        })
    }
}