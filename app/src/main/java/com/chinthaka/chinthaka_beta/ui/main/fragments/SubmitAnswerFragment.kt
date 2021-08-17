package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.data.entities.User
import com.chinthaka.chinthaka_beta.databinding.FragmentSubmitAnswerBinding
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.BasePostViewModel
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubmitAnswerFragment : BasePostFragment(R.layout.fragment_submit_answer) {

    private lateinit var submitAnswerFragmentBinding: FragmentSubmitAnswerBinding

    private val args: SubmitAnswerFragmentArgs by navArgs()

    private lateinit var answer: String

    private var currentUser: User? = null

    private var helpsRemaining: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        answer = args.answer

        helpsRemaining = answer.length/3

        submitAnswerFragmentBinding = FragmentSubmitAnswerBinding.bind(view)

        submitAnswerFragmentBinding.btnSubmit.isClickable = false

        submitAnswerFragmentBinding.tvCorrectAnswer.text = prepareTextForCorrectAnswer(answer, "")

        submitAnswerFragmentBinding.etUserAnswer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val userAnswer = charSequence.toString()
                submitAnswerFragmentBinding.tvCorrectAnswer.text = prepareTextForCorrectAnswer(
                    answer,
                    userAnswer
                )
                if (userAnswer.equals(answer, ignoreCase = true)) {
                    hideSoftKeyboard(requireActivity())
                    submitAnswerFragmentBinding.etUserAnswer.inputType = InputType.TYPE_NULL
                    submitAnswerFragmentBinding.btnSubmit.isClickable = true
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        submitAnswerFragmentBinding.btnGetHelp.setOnClickListener {
            if (helpsRemaining == 0) {
                submitAnswerFragmentBinding.btnGetHelp.isClickable = false
            }
            val userAnswer: String = submitAnswerFragmentBinding.etUserAnswer.text.toString()
            val userAnswerCharArray = userAnswer.lowercase().toCharArray()
            var i = 0
            for (ch in userAnswerCharArray) {
                if (ch == answer.lowercase().get(i)) {
                    i++
                } else {
                    break
                }
            }
            submitAnswerFragmentBinding.tvCorrectAnswer.text = prepareTextForCorrectAnswer(
                answer,
                answer.substring(0, i + 1)
            )
            submitAnswerFragmentBinding.etUserAnswer.setText(answer.substring(0, i + 1))
            submitAnswerFragmentBinding.etUserAnswer.setSelection(submitAnswerFragmentBinding.etUserAnswer.text.toString().length)
            helpsRemaining--
        }

        submitAnswerFragmentBinding.btnSubmit.setOnClickListener {
//            basePostViewModel.submitAnswerForPost()
            Toast.makeText(requireContext(), "Answer Submitted", Toast.LENGTH_SHORT).show()
        }

    }

    private fun prepareTextForCorrectAnswer(correctAnswer: String, userAnswer: String): String? {
        val sb = StringBuilder()
        var i = 0
        if (userAnswer.isNotEmpty()) {
            val userAnswerCharArray = userAnswer.lowercase().toCharArray()
            for (ch in userAnswerCharArray) {
                if (ch == correctAnswer.lowercase()[i]) {
                    if (ch == ' ') {
                        sb.append(ch).append("\n")
                    } else {
                        sb.append(correctAnswer[i]).append(" ")
                    }
                } else {
                    break
                }
                i++
            }
        }
        for (j in i until correctAnswer.length) {
            if (correctAnswer[j] != ' ') {
                sb.append("_ ")
            } else {
                sb.append("\n")
            }
        }
        return sb.toString()
    }

    private fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager: InputMethodManager = activity.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (inputMethodManager.isAcceptingText) {
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken,
                0
            )
        }
    }

    override val basePostViewModel: BasePostViewModel
        get() {
            val viewModel : HomeViewModel by viewModels()
            return viewModel
        }

    val userId: String
        get() = FirebaseAuth.getInstance().uid!!
}