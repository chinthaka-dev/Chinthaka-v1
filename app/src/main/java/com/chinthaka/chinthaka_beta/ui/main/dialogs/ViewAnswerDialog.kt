package com.chinthaka.chinthaka_beta.ui.main.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.chinthaka.chinthaka_beta.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ViewAnswerDialog: DialogFragment() {

    private var positiveListener: (() -> Unit)? = null

    fun setPositiveListener(listener: () -> Unit){
        positiveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(),R.style.AlertDialogCustom)
            .setMessage(R.string.view_answer_dialog_message)
            .setIcon(R.drawable.ic_view_answer_outline)
            .setPositiveButton(R.string.view_answer_dialog_positive){ _, _ ->
                positiveListener?.let{ click ->
                    click()
                }
            }
            .setNegativeButton(R.string.view_answer_dialog_negative){ dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
    }
}