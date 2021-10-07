package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.data.entities.Post
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.databinding.FragmentBookmarksBinding
import com.chinthaka.chinthaka_beta.databinding.FragmentViewPostBinding
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.BasePostViewModel
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.BookmarksViewModel
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.ViewPostViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewPostFragment : BasePostFragment(R.layout.fragment_view_post) {

    private lateinit var fragmentBinding: FragmentViewPostBinding

    private val args: ViewPostFragmentArgs by navArgs()

    private val viewModel: ViewPostViewModel by lazy { basePostViewModel as ViewPostViewModel }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentBinding = FragmentViewPostBinding.bind(view)
        viewModel.postId = args.postId
        setUpRecyclerView()

        //Paging is being integrated
        lifecycleScope.launch {
            viewModel.pagingFlow.collect {
                postAdapter.submitData(it)
            }
        }

        // Progress Bar during Paging
        lifecycleScope.launch {
            postAdapter.loadStateFlow.collectLatest {
                fragmentBinding.viewPostProgressBar?.isVisible =
                    it.refresh is LoadState.Loading || it.append is LoadState.Loading
            }
        }

        postAdapter.setOnExpandClickListener{ post, i ->
            curBookmarkedIndex = i
            post.isBookmarked = !post.isBookmarked
            viewModel.toggleBookmarkForPost(post)
        }

    }

    private fun setUpRecyclerView() = fragmentBinding.rvAllPosts.apply {
        adapter = postAdapter
        layoutManager = LinearLayoutManager(requireContext())
        itemAnimator = null
    }

    override val basePostViewModel: BasePostViewModel
        get() {
            val viewModel : ViewPostViewModel by viewModels()
            return viewModel
        }
}