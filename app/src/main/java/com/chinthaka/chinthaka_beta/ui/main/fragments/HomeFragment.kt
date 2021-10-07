package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.databinding.FragmentHomeBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.listeners.NavigationUpdateListener
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.BasePostViewModel
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.HomeViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BasePostFragment(R.layout.fragment_home) {

    private lateinit var fragmentHomeBinding: FragmentHomeBinding

    private val viewModel: HomeViewModel by lazy { basePostViewModel as HomeViewModel }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentHomeBinding = FragmentHomeBinding.bind(view)

        setUpRecyclerView()
        subscribeToObservers()

        viewModel.getUser(FirebaseAuth.getInstance().uid!!)

        //Paging is being integrated
        lifecycleScope.launch {
            viewModel.pagingFlow.collect {
                postAdapter.submitData(it)
            }
        }

        // Prorgress Bar during Paging
        lifecycleScope.launch {
            postAdapter.loadStateFlow.collectLatest {
                fragmentHomeBinding.allPostsProgressBar?.isVisible =
                    it.refresh is LoadState.Loading || it.append is LoadState.Loading
            }
        }

    }

    private fun subscribeToObservers() {
        viewModel.getUserStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                fragmentHomeBinding.userProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {fragmentHomeBinding.userProgressBar.isVisible = true }
        ) { user ->
            fragmentHomeBinding.userProgressBar.isVisible = false
            (requireActivity() as NavigationUpdateListener).onUserDataChanged(user)
        })
    }

    private fun setUpRecyclerView() = fragmentHomeBinding.rvAllPosts.apply {
        adapter = postAdapter
        layoutManager = LinearLayoutManager(requireContext())
        itemAnimator = null
    }

    override val basePostViewModel: BasePostViewModel
        get() {
            val viewModel : HomeViewModel by viewModels()
            return viewModel
        }
}