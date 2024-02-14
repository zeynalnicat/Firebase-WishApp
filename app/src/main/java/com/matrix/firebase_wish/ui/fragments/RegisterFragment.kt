package com.matrix.firebase_wish.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.matrix.firebase_wish.R
import com.matrix.firebase_wish.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)
        adaptLayout()
        setNavigation()
        setRegister()
        return binding.root
    }


    private fun adaptLayout() {
        binding.txtHeader.text = "Register"
        binding.button.text = "Register"
        binding.edtUsername.text.clear()
        binding.edtPassword.text.clear()

        binding.txtNotHave.text = "Have an account? "
    }

    private fun setNavigation() {
        binding.txtNotHave.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setRegister(){
        binding.button.setOnClickListener {
            val email = binding.edtUsername.text.toString()
            val password = binding.edtPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Fill the user credentials", Toast.LENGTH_SHORT)
                    .show()
            } else {
                firebaseAuth
                    .createUserWithEmailAndPassword(email,password)
                    .addOnSuccessListener {
                        findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),"Something wrong happened! Try later!", Toast.LENGTH_SHORT).show()
                    }

            }
        }
    }



}