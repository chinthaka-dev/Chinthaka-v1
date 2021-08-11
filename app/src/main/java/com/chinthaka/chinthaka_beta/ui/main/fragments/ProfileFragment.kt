package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.databinding.FragmentProfileBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.BasePostViewModel
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.ProfileViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
open class ProfileFragment : BasePostFragment(R.layout.fragment_profile) {

    private lateinit var fragmentProfileBinding: FragmentProfileBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentProfileBinding = FragmentProfileBinding.bind(view)

        setUpRecyclerView()
        subscribeToObservers()

//        fragmentProfileBinding.btnToggleFollow.isVisible = false
        viewModel.loadProfile(userId)

        //Paging is being integrated
        lifecycleScope.launch {
            viewModel.getPagingFlow(userId).collect {
                postAdapter.submitData(it)
            }
        }

        // Prorgress Bar during Paging
        lifecycleScope.launch {
            postAdapter.loadStateFlow.collectLatest {
                fragmentProfileBinding.profilePostsProgressBar?.isVisible =
                    it.refresh is LoadState.Loading || it.append is LoadState.Loading
            }
        }
    }

    private fun setUpRecyclerView() = fragmentProfileBinding.rvPosts.apply {
        adapter = postAdapter
        itemAnimator = null
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObservers(){
        viewModel.profileMeta.observe(viewLifecycleOwner, EventObserver(
            onError = {
                fragmentProfileBinding.profileMetaProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = { fragmentProfileBinding.profileMetaProgressBar.isVisible = true }
        ) { user ->
            fragmentProfileBinding.profileMetaProgressBar.isVisible = false
            fragmentProfileBinding.tvUsername.text = user.userName
            fragmentProfileBinding.tvProfileDescription.text = if(user.description.isEmpty()){
                requireContext().getString(R.string.no_description)
            } else user.description
            glide.load(user.profilePictureUrl).into(fragmentProfileBinding.ivProfileImage)
        })

        basePostViewModel.deletePostStatus.observe(viewLifecycleOwner, EventObserver(
            onError = { snackbar(it) }
        ) { deletedPost ->
            postAdapter.refresh() // Downside here is that it reloads all posts
        })
    }

    override val basePostViewModel: BasePostViewModel
        get() {
            val viewModel: ProfileViewModel by viewModels()
            return viewModel
        }

    protected val viewModel: ProfileViewModel
        get() = basePostViewModel as ProfileViewModel

    protected open val userId: String
        get() = FirebaseAuth.getInstance().uid!!
}