package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.chinthaka.chinthaka_beta.databinding.FragmentInviteFriendsBinding
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.data.entities.Metric
import com.chinthaka.chinthaka_beta.repositories.MetricRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InviteFriendsFragment: Fragment(R.layout.fragment_invite_friends) {

    private lateinit var fragmentBinding: FragmentInviteFriendsBinding

    val metricRepository = MetricRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentBinding = FragmentInviteFriendsBinding.bind(view)

        fragmentBinding.btnInvite.setOnClickListener(View.OnClickListener {
            val intent: Intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT,
                "${FirebaseAuth.getInstance().currentUser?.displayName} has invited you to install Chinthaka - Answer, Learn, & Share questions from a wide variety of topics! "
                    + "http://play.google.com")
            metricRepository.recordClicksOnMetric(Metric.CLICKS_ON_INVITE)
            startActivity(Intent.createChooser(intent, "Share using"))
        })

    }

}