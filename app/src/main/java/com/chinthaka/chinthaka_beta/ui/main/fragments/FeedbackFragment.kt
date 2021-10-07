package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.databinding.FragmentFeedbackBinding
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import android.net.Uri


@AndroidEntryPoint
class FeedbackFragment: Fragment(R.layout.fragment_feedback) {

    private lateinit var fragmentBinding: FragmentFeedbackBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentBinding = FragmentFeedbackBinding.bind(view)

        fragmentBinding.btnFeedbackEmail.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "plain/text"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support.chinthaka@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback - Chinthaka App")
            startActivity(Intent.createChooser(intent, "Email Feedback"))
        })

        fragmentBinding.btnFeedbackPlayStore.setOnClickListener(View.OnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=pradyumna.simhansapp")))
        })
    }
}