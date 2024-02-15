package com.matrix.firebase_wish.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.matrix.firebase_wish.R
import com.matrix.firebase_wish.databinding.FragmentAccountBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject



@AndroidEntryPoint
class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    private var name: Any? = null
    private var img :Any? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater)
        setLayout()
        setNavigation()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLogOut()
    }

    private fun setLayout() {
        val userRef = firestore.collection("users")
        val query = userRef.whereEqualTo("userId", firebaseAuth.currentUser?.uid)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]

                    name = document.getString("name")
                    img = document.getString("img")

                    binding.edtName.setText(name.toString())

                    if (img.toString().isEmpty()) {
                        binding.imgProfile.setImageResource(R.drawable.man_icon)
                    } else {
                        Glide.with(binding.root)
                            .load(img)
                            .into(binding.imgProfile)
                    }
                } else {

                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLogOut() {
        binding.btnLogout.setOnClickListener {
            firebaseAuth
                .signOut()
            findNavController().navigate(R.id.action_accountFragment_to_loginFragment)

        }

    }

    private fun setNavigation() {
        binding.txtSave.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_editAccountFragment)
        }
    }
}

