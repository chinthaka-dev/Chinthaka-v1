package com.chinthaka.chinthaka_beta.ui.main.fragments


import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.adapters.UserAdapter
import com.chinthaka.chinthaka_beta.databinding.FragmentSearchBinding
import com.chinthaka.chinthaka_beta.databinding.FragmentUserBinding
import com.chinthaka.chinthaka_beta.other.Constants.SEARCH_TIME_DELAY
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.SearchViewModel
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.UsersViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UsersFragment : Fragment(R.layout.fragment_user) {

    @Inject
    lateinit var userAdapter: UserAdapter

    private lateinit var fragmentUserBinding: FragmentUserBinding

    private val viewModel: UsersViewModel by viewModels()

    private val args: UsersFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentUserBinding = FragmentUserBinding.bind(view)

        val userIds = args.userIds.toList()

        setupRecyclerView()
        subscribeToObservers()

        viewModel.getUsers(userIds)

    }

    private fun subscribeToObservers(){
        viewModel.searchResults.observe(viewLifecycleOwner, EventObserver(
            onError = {
                fragmentUserBinding.searchProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                fragmentUserBinding.searchProgressBar.isVisible = true
            }
        ) { users ->
            fragmentUserBinding.searchProgressBar.isVisible = false
            userAdapter.users = users
        })
    }

    private fun setupRecyclerView() = fragmentUserBinding.rvSearchResults.apply {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = userAdapter
        itemAnimator = null

    }
}