package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.databinding.FragmentSubmitAnswerBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.main.viewmodels.SubmitAnswerViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates

@AndroidEntryPoint
class SubmitAnswerFragment : Fragment(R.layout.fragment_submit_answer) {

    private lateinit var submitAnswerFragmentBinding: FragmentSubmitAnswerBinding

    private val args: SubmitAnswerFragmentArgs by navArgs()

    private val viewModel: SubmitAnswerViewModel by viewModels()

    private lateinit var answer: String

    private var curAnsweredIndex by Delegates.notNull<Int>()

    private var helpsRemaining: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()

        answer = args.answer

        curAnsweredIndex = args.currentIndex

        helpsRemaining = answer.length / 3

        submitAnswerFragmentBinding = FragmentSubmitAnswerBinding.bind(view)

//        submitAnswerFragmentBinding.btnSubmit.isClickable = false

        submitAnswerFragmentBinding.tvCorrectAnswer.text = prepareTextForCorrectAnswer(answer, "")

        submitAnswerFragmentBinding.etUserAnswer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val userAnswer = charSequence.toString()
                submitAnswerFragmentBinding.tvCorrectAnswer.text = prepareTextForCorrectAnswer(
                    answer,
                    userAnswer
                )
                if (userAnswer.equals(answer, ignoreCase = true)) {
                    hideSoftKeyboard(requireActivity())
                    submitAnswerFragmentBinding.etUserAnswer.inputType = InputType.TYPE_NULL
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
                if (ch == answer.lowercase()[i]) {
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
            viewModel.submitAnswerForPost(postId = args.postId)
        }

        submitAnswerFragmentBinding.tvAnswerDetails.text = prepareAnswerDetails(answer)

    }

    private fun prepareTextForCorrectAnswer(correctAnswer: String, userAnswer: String): String? {
        val sb = StringBuilder()
        var i = 0
        if (userAnswer.isNotEmpty()) {
            val userAnswerCharArray = userAnswer.lowercase().toCharArray()
            for (ch in userAnswerCharArray) {
                if (ch == correctAnswer.lowercase()[i]) {
                    if (ch == ' ') {
                        sb.append(ch).append("   ")
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
                sb.append("   ")
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

    private fun prepareAnswerDetails(correctAnswer: String): String? {
        val sb = StringBuilder()
        sb.append("Words: ")
            .append(calculateWordsForAnswer(correctAnswer))
            .append(", Characters: ")
            .append(calculateCharactersForAnswer(correctAnswer))
        return sb.toString()
    }

    private fun calculateWordsForAnswer(correctAnswer: String): Int {
        val chars = correctAnswer.toCharArray()
        var countOfWords = 1;
        for (ch in chars) {
            if (ch == ' ' || ch == '\n') {
                countOfWords++
            }
        }
        return countOfWords
    }

    private fun calculateCharactersForAnswer(correctAnswer: String): Int {
        val chars = correctAnswer.toCharArray()
        var countOfChars = 0;
        for (ch in chars) {
            if (ch != ' ' || ch == '\n') {
                countOfChars++
            }
        }
        return countOfChars
    }

    private fun subscribeToObservers() {
        viewModel.answerPostStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                submitAnswerFragmentBinding.submitAnswerProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                submitAnswerFragmentBinding.submitAnswerProgressBar.isVisible = true
            }
        ) { isAnswered ->
            submitAnswerFragmentBinding.submitAnswerProgressBar.isVisible = false
            snackbar(requireContext().getString(R.string.answer_submitted))
        })
    }

    val userId: String
        get() = FirebaseAuth.getInstance().uid!!
}