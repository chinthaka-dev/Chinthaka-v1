package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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
import com.chinthaka.chinthaka_beta.databinding.FragmentProfileBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.listeners.NavigationUpdateListener
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.ProfileViewModel
import com.chinthaka.chinthaka_beta.ui.slideUpViews
import com.chinthaka.chinthaka_beta.ui.snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var fragmentProfileBinding: FragmentProfileBinding

    private val viewModel: ProfileViewModel by viewModels()

    private var curImageUri: Uri? = null

    private val args: ProfileFragmentArgs by navArgs()

    private lateinit var cropContent: ActivityResultLauncher<Any?>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentProfileBinding = FragmentProfileBinding.bind(view)

        val isNavigatedFromDrawer: Boolean = args.isNavigatedFromDrawer

        subscribeToObservers()
        val userId = FirebaseAuth.getInstance().uid!!
        viewModel.getUser(userId)

        fragmentProfileBinding.etUsername.addTextChangedListener{
            if(fragmentProfileBinding.etUsername.text!!.isNotEmpty() &&
                    fragmentProfileBinding.etDescription.text!!.isNotEmpty()){
                fragmentProfileBinding.btnUpdateProfile.isEnabled = true
            }
        }

        fragmentProfileBinding.etDescription.addTextChangedListener{
            if(fragmentProfileBinding.etUsername.text!!.isNotEmpty() &&
                fragmentProfileBinding.etDescription.text!!.isNotEmpty()){
                fragmentProfileBinding.btnUpdateProfile.isEnabled = true
            }
        }

        fragmentProfileBinding.ivProfileImage.setOnClickListener {
            cropContent.launch(null)
        }

        fragmentProfileBinding.btnUpdateProfile.setOnClickListener {
            val userName = fragmentProfileBinding.etUsername.text.toString()
            val description = fragmentProfileBinding.etDescription.text.toString()
            val profileUpdate = ProfileUpdate(userId, userName, description, curImageUri)
            viewModel.updateProfile(profileUpdate)
            if(!isNavigatedFromDrawer){
                findNavController().navigate(R.id.action_profileFragment_to_InterestsFragment)
            }
        }

        slideUpViews(
            requireContext(),
            fragmentProfileBinding.ivProfileImage,
            fragmentProfileBinding.etUsername,
            fragmentProfileBinding.etDescription,
            fragmentProfileBinding.btnUpdateProfile
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropContent = registerForActivityResult(cropActivityResultContract) { uri ->
            uri?.let {
                viewModel.setCurImageUri(it)
                fragmentProfileBinding.btnUpdateProfile.isEnabled = true
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
                fragmentProfileBinding.profileProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {fragmentProfileBinding.profileProgressBar.isVisible = true }
        ) { user ->
            fragmentProfileBinding.profileProgressBar.isVisible = false
            glide.load(user.profilePictureUrl).into(fragmentProfileBinding.ivProfileImage)
            fragmentProfileBinding.etUsername.setText(user.userName)
            fragmentProfileBinding.etDescription.setText(user.description)
            (requireActivity() as NavigationUpdateListener).onUserDataChanged(user)
        })

        viewModel.curImageUri.observe(viewLifecycleOwner) { uri ->
            curImageUri = uri
            glide.load(uri).into(fragmentProfileBinding.ivProfileImage)
        }

        viewModel.updateProfileStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                fragmentProfileBinding.profileProgressBar.isVisible = false
                snackbar(it)
                fragmentProfileBinding.btnUpdateProfile.isEnabled = true
            },
            onLoading = {
                fragmentProfileBinding.profileProgressBar.isVisible = true
                fragmentProfileBinding.btnUpdateProfile.isEnabled = false
            }
        ) {
            fragmentProfileBinding.profileProgressBar.isVisible = false
            fragmentProfileBinding.btnUpdateProfile.isEnabled = false
            snackbar(requireContext().getString(R.string.profile_updated))
        })
    }
}