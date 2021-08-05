package com.chinthaka.chinthaka_beta.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.databinding.FragmentLoginBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.auth.AuthViewModel
import com.chinthaka.chinthaka_beta.ui.main.MainActivity
import com.chinthaka.chinthaka_beta.ui.snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var loginBinding: FragmentLoginBinding

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginBinding = FragmentLoginBinding.bind(view)

        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        subscribeToObservers()

        loginBinding.btnLogin.setOnClickListener{
            viewModel.login(
                loginBinding.etEmail.text.toString(),
                loginBinding.etPassword.text.toString()
            )
        }

        loginBinding.tvRegisterNewAccount.setOnClickListener {
            if(findNavController().previousBackStackEntry != null){
                // This is done so that fragments don't get repeatedly stacked up
                findNavController().popBackStack()
            } else findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            )
        }
    }

    private fun subscribeToObservers(){
        viewModel.loginStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                loginBinding.loginProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                loginBinding.loginProgressBar.isVisible = true
            }
        ){
            // On Successful Login, navigate User to Main Activity
            loginBinding.loginProgressBar.isVisible = false
            Intent(requireContext(), MainActivity::class.java).also {
                startActivity(it)
                requireActivity().finish() // Pop the Auth Activity from Back Stack
            }
        })
    }
}