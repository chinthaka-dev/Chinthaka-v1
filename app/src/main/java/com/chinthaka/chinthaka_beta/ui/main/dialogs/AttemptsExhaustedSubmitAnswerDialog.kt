package com.chinthaka.chinthaka_beta.ui.main.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.chinthaka.chinthaka_beta.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AttemptsExhaustedSubmitAnswerDialog: DialogFragment() {

    private var positiveListener: (() -> Unit)? = null

    private var negativeListener: (() -> Unit)? = null

    fun setPositiveListener(listener: () -> Unit){
        positiveListener = listener
    }

    fun setNegativeListener(listener: () -> Unit){
        negativeListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(),R.style.AlertDialogCustom)
            .setTitle("Oops!")
            .setMessage(R.string.attempts_exhausted_submit_answer_dialog_message)
            .setIcon(R.drawable.ic_logo)
            .setPositiveButton(R.string.view_answer_dialog_positive){ _, _ ->
                positiveListener?.let{ click ->
                    click()
                }
            }
            .setNegativeButton(R.string.view_answer_dialog_negative){ dialogInterface, _ ->
                dialogInterface.cancel()
                negativeListener?.let{ click ->
                    click()
                }
            }
            .create()
    }
}