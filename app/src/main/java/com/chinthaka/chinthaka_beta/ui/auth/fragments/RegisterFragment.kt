package com.chinthaka.chinthaka_beta.ui.auth.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.databinding.FragmentRegisterBinding
import com.chinthaka.chinthaka_beta.other.EventObserver
import com.chinthaka.chinthaka_beta.ui.auth.AuthViewModel
import com.chinthaka.chinthaka_beta.ui.snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var registerBinding: FragmentRegisterBinding

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerBinding = FragmentRegisterBinding.bind(view)

        viewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        subscribeToObservers()

        registerBinding.btnRegister.setOnClickListener{
            viewModel.register(
                registerBinding.etEmail.text.toString(),
                registerBinding.etUsername.text.toString(),
                registerBinding.etPassword.text.toString(),
                registerBinding.etRepeatPassword.text.toString()
            )
        }

        registerBinding.tvLogin.setOnClickListener {
            if(findNavController().previousBackStackEntry != null){
                // This is done so that fragments don't get repeatedly stacked up
                findNavController().popBackStack()
            } else findNavController().navigate(
                RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            )
        }
    }

    private fun subscribeToObservers(){
        viewModel.registerStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                registerBinding.registerProgressBar.isVisible = false
                snackbar(it)
            },
            onLoading = {
                registerBinding.registerProgressBar.isVisible = true
            }
        ){
            registerBinding.registerProgressBar.isVisible = false
            snackbar(getString(R.string.success_registration))
        })
    }
}