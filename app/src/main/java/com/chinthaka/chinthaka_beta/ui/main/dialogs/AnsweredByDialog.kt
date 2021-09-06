package com.chinthaka.chinthaka_beta.ui.main.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.adapters.UserAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AnsweredByDialog(
    private val userAdapter: UserAdapter
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val rvAnsweredBy = RecyclerView(requireContext()).apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        return MaterialAlertDialogBuilder(requireContext(),R.style.AlertDialogCustom)
            .setTitle(R.string.answered_by_dialog_title)
            .setView(rvAnsweredBy)
            .create()
    }
}