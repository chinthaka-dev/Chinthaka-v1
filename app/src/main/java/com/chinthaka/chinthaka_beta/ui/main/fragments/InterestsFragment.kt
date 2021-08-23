package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.adapters.CategoryAdapter
import com.chinthaka.chinthaka_beta.data.entities.CategoryUpdate
import com.chinthaka.chinthaka_beta.databinding.FragmentInterestsBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.InterestsViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InterestsFragment : Fragment(R.layout.fragment_interests) {

    @Inject
    lateinit var categoryAdapter: CategoryAdapter

    private lateinit var fragmentInterestsBinding: FragmentInterestsBinding

    private val viewModel: InterestsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentInterestsBinding = FragmentInterestsBinding.bind(view)

        setUpRecyclerView()
        subscribeToObservers()

        val user = FirebaseAuth.getInstance().currentUser!!

        viewModel.loadInterests()

        categoryAdapter.setOnViewClickListener { i ->
            categoryAdapter.categories[i].isSelected = !categoryAdapter.categories[i].isSelected
            categoryAdapter.notifyItemChanged(i)
        }

        fragmentInterestsBinding.btnDone.setOnClickListener {
            val categoryUpdate = CategoryUpdate(user.uid,
                categoryAdapter.categories.filter { category -> category.isSelected }
                    .map { category -> category.categoryName })
            viewModel.updateInterests(categoryUpdate)
        }
    }

    private fun setUpRecyclerView() = fragmentInterestsBinding.chooseInterestsRecyclerView.apply {
        adapter = categoryAdapter
        itemAnimator = null
        layoutManager = GridLayoutManager(context, 2)
    }

    private fun subscribeToObservers() {
        viewModel.interests.observe(viewLifecycleOwner, EventObserver(
            onError = {
                fragmentInterestsBinding.allInterestsProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = { fragmentInterestsBinding.allInterestsProgressBar.isVisible = true }
        ) { interests ->
            fragmentInterestsBinding.allInterestsProgressBar.isVisible = false
            categoryAdapter.categories = interests
        })

        viewModel.submitInterestsStatus.observe(this, EventObserver(
            onError = { snackbar(it) },
            onLoading = { fragmentInterestsBinding.allInterestsProgressBar.isVisible = true }
        ) {
            findNavController().navigate(InterestsFragmentDirections.actionInterestsFragmentToHomeFragment())
        })
    }
}