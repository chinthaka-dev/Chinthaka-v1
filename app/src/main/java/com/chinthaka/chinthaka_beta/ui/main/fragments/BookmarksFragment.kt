package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.databinding.FragmentBookmarksBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.BasePostViewModel
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.BookmarksViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookmarksFragment : BasePostFragment(R.layout.fragment_bookmarks) {

    private lateinit var fragmentBookmarksBinding: FragmentBookmarksBinding

    private val viewModel: BookmarksViewModel by lazy { basePostViewModel as BookmarksViewModel }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentBookmarksBinding = FragmentBookmarksBinding.bind(view)

        setUpRecyclerView()

       /* //Paging is being integrated
        lifecycleScope.launch {
            viewModel.pagingFlow.collect {
                postAdapter.submitData(it)
            }
        }

        // Progress Bar during Paging
        lifecycleScope.launch {
            postAdapter.loadStateFlow.collectLatest {
                fragmentBookmarksBinding.bookmarksPostsProgressBar?.isVisible =
                    it.refresh is LoadState.Loading || it.append is LoadState.Loading
            }
        }*/

        postAdapter.setOnExpandClickListener{ post, i ->
            curBookmarkedIndex = i
            post.isBookmarked = !post.isBookmarked
            viewModel.toggleBookmarkForPost(post)
        }

    }

    private fun setUpRecyclerView() = fragmentBookmarksBinding.rvAllPosts.apply {
        adapter = postAdapter
        layoutManager = LinearLayoutManager(requireContext())
        itemAnimator = null
    }

    override val basePostViewModel: BasePostViewModel
        get() {
            val viewModel : BookmarksViewModel by viewModels()
            return viewModel
        }
}