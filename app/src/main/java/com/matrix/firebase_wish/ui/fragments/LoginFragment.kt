package com.matrix.firebase_wish.ui.fragments

import android.content.Context
import android.content.SharedPreferences
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
import com.matrix.firebase_wish.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding


    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private var sharedPreference: SharedPreferences? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)
        bottomNavigation()
        setNavigation()
        setLogin()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreference = activity?.getSharedPreferences("UserDetail", Context.MODE_PRIVATE)

        if (firebaseAuth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

    }


    private fun setLogin() {
        binding.button.setOnClickListener {
            val email = binding.edtUsername.text.toString()
            val password = binding.edtPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Fill the user credentials", Toast.LENGTH_SHORT)
                    .show()
            } else {
                firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),"Wrong Email or Password",Toast.LENGTH_SHORT).show()
                    }

            }
        }
    }


    private fun bottomNavigation() {
        val activity = requireActivity() as MainActivity
        activity.setBottomNavigation(false)
    }

    private fun setNavigation() {
        binding.txtNotHave.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

    }
}