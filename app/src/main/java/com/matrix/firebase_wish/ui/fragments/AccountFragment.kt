package com.matrix.firebase_wish.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.matrix.firebase_wish.R
import com.matrix.firebase_wish.databinding.FragmentAccountBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding
    private var sPreferences: SharedPreferences? = null

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater)
        sPreferences = activity?.getSharedPreferences("UserDetail", Context.MODE_PRIVATE)
        setSharedPreference()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLogOut()
    }

    private fun setSharedPreference() {
        sPreferences?.let {
            binding.txtName.text = it.getString("Name", "N/A")
            binding.txtUsername.text = firebaseAuth.currentUser?.email
            Glide.with(binding.root).load(it.getString("ImgProfile", "")).into(binding.imgProfile)
        }
    }

    private fun setLogOut() {
        binding.btnLogOut.setOnClickListener {
            firebaseAuth
                .signOut()
            findNavController().navigate(R.id.action_accountFragment_to_loginFragment)

        }

    }
}

