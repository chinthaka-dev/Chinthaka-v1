package com.chinthaka.chinthaka_beta.ui.main.fragments

import android.app.Activity
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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
import android.view.Gravity
import androidx.core.content.ContextCompat.getSystemService
import com.chinthaka.chinthaka_beta.data.entities.Metric
import com.chinthaka.chinthaka_beta.repositories.MetricRepository


@AndroidEntryPoint
class SubmitAnswerFragment : Fragment(R.layout.fragment_submit_answer) {

    private lateinit var submitAnswerFragmentBinding: FragmentSubmitAnswerBinding

    private val args: SubmitAnswerFragmentArgs by navArgs()

    private val viewModel: SubmitAnswerViewModel by viewModels()

    private lateinit var answer: String

    private var curAnsweredIndex by Delegates.notNull<Int>()

    private var hintsRemaining: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val metricRepository = MetricRepository()
        metricRepository.recordClicksOnMetric(Metric.NUMBER_OF_ATTEMPTS)

        subscribeToObservers()

        answer = args.answer

        curAnsweredIndex = args.currentIndex

        hintsRemaining = answer.length / 3

        submitAnswerFragmentBinding = FragmentSubmitAnswerBinding.bind(view)

        submitAnswerFragmentBinding.tvCorrectAnswer.text = prepareTextForCorrectAnswer(answer, "")

        submitAnswerFragmentBinding.tvHintsRemaining.text = "Hints Remaining: $hintsRemaining"

        submitAnswerFragmentBinding.etUserAnswer.requestFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(submitAnswerFragmentBinding.etUserAnswer, InputMethodManager.SHOW_IMPLICIT)

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
                    viewModel.submitAnswerForPost(postId = args.postId)
                    metricRepository.recordClicksOnMetric(Metric.NUMBER_OF_ANSWERS_SUBMITTED)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        submitAnswerFragmentBinding.btnGetHint.setOnClickListener {
            if (hintsRemaining == 0) {
                submitAnswerFragmentBinding.btnGetHint.isClickable = false
                val toast = Toast.makeText(context, "Hints Exhausted!", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER or Gravity.CENTER_HORIZONTAL, 0, 0)
                toast.show()
                return@setOnClickListener
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
            hintsRemaining--
            submitAnswerFragmentBinding.tvHintsRemaining.text = "Hints Remaining: $hintsRemaining"
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
                        sb.append(ch).append(" ")
                    } else {
                        sb.append(correctAnswer[i])
                    }
                } else {
                    break
                }
                i++
            }
        }
        for (j in i until correctAnswer.length) {
            if (correctAnswer[j] != ' ') {
                sb.append("_")
            } else {
                sb.append(" ")
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