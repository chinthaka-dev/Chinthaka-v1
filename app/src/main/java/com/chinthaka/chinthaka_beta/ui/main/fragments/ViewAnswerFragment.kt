package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.data.entities.Metric
import com.chinthaka.chinthaka_beta.databinding.FragmentViewAnswerBinding
import com.chinthaka.chinthaka_beta.repositories.MetricRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController





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

        val toolbar: Toolbar = requireActivity().findViewById(com.chinthaka.chinthaka_beta.R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigate(ViewAnswerFragmentDirections.actionViewAnswerFragmentToHomeFragment())
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do custom work here
                findNavController().navigate(ViewAnswerFragmentDirections.actionViewAnswerFragmentToHomeFragment())
            }
        })
    }

}