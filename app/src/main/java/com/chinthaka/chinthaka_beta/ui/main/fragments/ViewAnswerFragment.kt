package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.data.entities.Metric
import com.chinthaka.chinthaka_beta.databinding.FragmentProfileBinding
import com.chinthaka.chinthaka_beta.databinding.FragmentViewAnswerBinding
import com.chinthaka.chinthaka_beta.repositories.MetricRepository
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.BasePostViewModel
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ViewAnswerFragment: Fragment(R.layout.fragment_view_answer) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var fragmentViewAnswerBinding: FragmentViewAnswerBinding

    private val args: ViewAnswerFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val metricRepository = MetricRepository()
        metricRepository.recordClicksOnMetric(Metric.CLICKS_ON_VIEW_SOLUTIONS)

        fragmentViewAnswerBinding = FragmentViewAnswerBinding.bind(view)

        fragmentViewAnswerBinding.answer.text = args.answer
        if(args.description.isEmpty()){
            fragmentViewAnswerBinding.explanationTitle.isVisible = false
            fragmentViewAnswerBinding.explanationAnswer.isVisible = false
        }
        else {
            fragmentViewAnswerBinding.explanationAnswer.text = args.description
        }
        if(args.imageUrl.isNullOrBlank()){
            fragmentViewAnswerBinding.explanationImage.isVisible = false
        } else glide.load(args.imageUrl).into(fragmentViewAnswerBinding.explanationImage)
    }
}