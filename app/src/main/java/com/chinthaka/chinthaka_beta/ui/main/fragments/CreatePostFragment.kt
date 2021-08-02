package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.databinding.FragmentCreatePostBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.CreatePostViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreatePostFragment : Fragment(R.layout.fragment_create_post) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var fragmentCreatePostBinding: FragmentCreatePostBinding

    private val viewModel: CreatePostViewModel by viewModels()

    private var curImageUri: Uri? = null

    private lateinit var cropContent: ActivityResultLauncher<Any?>

    private val cropActivityResultContract = object : ActivityResultContract<Any?, Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {
            // This fun returns the Intent which will launch the CROP Activity

            return CropImage.activity()
                .setAspectRatio(16, 9)
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(requireContext())
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            // This fun returns Output Type (URI)

            return CropImage.getActivityResult(intent)?.uriContent
        }
    }  // Getting result from the CROP activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropContent = registerForActivityResult(cropActivityResultContract){
            it?.let{
                viewModel.setCurImageUri(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentCreatePostBinding = FragmentCreatePostBinding.bind(view)

        subscribeToObservers()

        fragmentCreatePostBinding.btnSetPostImage.setOnClickListener{
            cropContent.launch(null)
        }

        fragmentCreatePostBinding.ivPostImage.setOnClickListener{
            cropContent.launch(null)
        }

        fragmentCreatePostBinding.btnPost.setOnClickListener {
            curImageUri?.let { uri ->
                viewModel.createPost(uri, fragmentCreatePostBinding.etPostDescription.text.toString())
            } ?: snackbar(getString(R.string.error_no_image_chosen))
        }
    }

    private fun subscribeToObservers(){

        viewModel.curImageUri.observe(viewLifecycleOwner){
            curImageUri = it
            fragmentCreatePostBinding.btnSetPostImage.isVisible = false
            glide.load(curImageUri).into(fragmentCreatePostBinding.ivPostImage)
        }

        viewModel.createPostStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                fragmentCreatePostBinding.createPostProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                fragmentCreatePostBinding.createPostProgressBar.isVisible = true
            }
        ) {
            fragmentCreatePostBinding.createPostProgressBar.isVisible = false
            findNavController().popBackStack() // Once a post has been created, there is no point the user needs to stay in this fragment
        })
    }
}