package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.graphics.Color
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.databinding.FragmentProfileBinding
import com.chinthaka.chinthaka_beta.other.EventObserver

class OthersProfileFragment : ProfileFragment() {

    private lateinit var fragmentOthersProfileBinding: FragmentProfileBinding

    private val args: OthersProfileFragmentArgs by navArgs()

    override val userId: String
        get() = args.userId

    private var currentUser: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentOthersProfileBinding = FragmentProfileBinding.bind(view)

        subscribeToObservers()

//        fragmentOthersProfileBinding.btnToggleFollow.setOnClickListener {
//            viewModel.toggleFollowForUser(userId)
//        }
    }

    private fun subscribeToObservers() {

        viewModel.profileMeta.observe(viewLifecycleOwner, EventObserver{
//            fragmentOthersProfileBinding.btnToggleFollow.isVisible = true
//            setupToggleFollowButton(it)
            currentUser = it
        })

        viewModel.followStatus.observe(viewLifecycleOwner, EventObserver{
            currentUser?.isFollowing = it
//            setupToggleFollowButton(currentUser ?: return@EventObserver)
        })
    }

    /**
    private fun setupToggleFollowButton(user: User) {
        fragmentOthersProfileBinding.btnToggleFollow.apply {
            val changeBounds = ChangeBounds().apply {
                duration = 300
                interpolator  = OvershootInterpolator()
            }

            val set1 = ConstraintSet()
            val set2 = ConstraintSet()
            set1.clone(requireContext(), R.layout.fragment_profile)
            set2.clone(requireContext(), R.layout.fragment_profile_anim)
            TransitionManager.beginDelayedTransition(fragmentOthersProfileBinding.clProfile, changeBounds)

            if(user.isFollowing) {
                text = requireContext().getString(R.string.unfollow)
                setBackgroundColor(Color.RED)
                setTextColor(Color.WHITE)
                set1.applyTo(fragmentOthersProfileBinding.clProfile)
            } else{
                text = requireContext().getString(R.string.follow)
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
//                setTextColor(ContextCompat.getColor(requireContext(), R.color.darkBackground))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.lightBackground))
                set2.applyTo(fragmentOthersProfileBinding.clProfile)
            }
        }
    } */
}