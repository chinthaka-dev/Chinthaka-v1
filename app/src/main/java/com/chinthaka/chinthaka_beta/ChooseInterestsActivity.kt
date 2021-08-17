package com.chinthaka.chinthaka_beta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.chinthaka.chinthaka_beta.adapters.CategoryAdapter
import com.chinthaka.chinthaka_beta.data.entities.CategoryUpdate
import com.chinthaka.chinthaka_beta.data.entities.ProfileUpdate
import com.chinthaka.chinthaka_beta.databinding.ActivityChooseInterestsBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.MainActivity
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.InterestsViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
open class ChooseInterestsActivity : AppCompatActivity() {

    private lateinit var chooseInterestsBinding: ActivityChooseInterestsBinding

    private lateinit var viewModel: InterestsViewModel

    @Inject
    lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chooseInterestsBinding = ActivityChooseInterestsBinding.inflate(layoutInflater)
        setContentView(chooseInterestsBinding.root)

        viewModel = ViewModelProvider(this).get(InterestsViewModel::class.java)

        setUpRecyclerView()
        subscribeToObservers()

        val user = FirebaseAuth.getInstance().currentUser!!

        viewModel.loadInterests()

        categoryAdapter.setOnViewClickListener { category, i ->
            category.isSelected = !category.isSelected
            categoryAdapter.notifyItemChanged(i)
        }

        chooseInterestsBinding.btnDone.setOnClickListener {
            val categoryUpdate = CategoryUpdate(user.uid,
                categoryAdapter.categories.filter { category -> category.isSelected }.map { category -> category.categoryName })
            Log.i("INTERESTS_ACTIVITY", "Interests : ${categoryUpdate.updatedInterests}")
            viewModel.updateInterests(categoryUpdate)
        }
    }

    private fun setUpRecyclerView() = chooseInterestsBinding.chooseInterestsRecyclerView.apply {
        adapter = categoryAdapter
        itemAnimator = null
        layoutManager = GridLayoutManager(context, 2)
    }

    private fun subscribeToObservers() {
        viewModel.interests.observe(this, EventObserver(
            onError = {
                chooseInterestsBinding.allInterestsProgressBar.isVisible = false
//                snackbar(it)
            },
            onLoading = { chooseInterestsBinding.allInterestsProgressBar.isVisible = true }
        ) { interests ->
            chooseInterestsBinding.allInterestsProgressBar.isVisible = false
            categoryAdapter.categories = interests
        })

        viewModel.submitInterestsStatus.observe(this, EventObserver(
            onError = { Log.i("INTERESTS_ACTIVITY", "Error while Updating Interests") },
            onLoading = { chooseInterestsBinding.allInterestsProgressBar.isVisible = true }
        ) {
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        })
    }
}