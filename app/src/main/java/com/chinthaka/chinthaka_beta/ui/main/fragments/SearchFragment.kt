package com.chinthaka.chinthaka_beta.ui.main.fragments


import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.adapters.UserAdapter
import com.chinthaka.chinthaka_beta.databinding.FragmentSearchBinding
import com.chinthaka.chinthaka_beta.other.Constants.SEARCH_TIME_DELAY
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.SearchViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    @Inject
    lateinit var userAdapter: UserAdapter

    private lateinit var fragmentSearchBinding: FragmentSearchBinding

    private val viewModel: SearchViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentSearchBinding = FragmentSearchBinding.bind(view)

        setupRecyclerView()
        subscribeToObservers()

        var job: Job? = null
        fragmentSearchBinding.etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = lifecycleScope.launch{
                delay(SEARCH_TIME_DELAY)
                editable?.let{
                    viewModel.searchUser(it.toString())
                }
            }
        }

//        userAdapter.setOnUserClickListener {  user ->
//            findNavController()
//                .navigate(
//                    SearchFragmentDirections.globalActionToOthersProfileFragment(user.userId)
//                )
//        }
    }

    private fun subscribeToObservers(){
        viewModel.searchResults.observe(viewLifecycleOwner, EventObserver(
            onError = {
                fragmentSearchBinding.searchProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                fragmentSearchBinding.searchProgressBar.isVisible = true
            }
        ) { users ->
            fragmentSearchBinding.searchProgressBar.isVisible = false
            userAdapter.users = users
        })
    }

    private fun setupRecyclerView() = fragmentSearchBinding.rvSearchResults.apply {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = userAdapter
        itemAnimator = null
    }
}