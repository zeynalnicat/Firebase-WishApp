package com.matrix.firebase_wish.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
class EditAccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding
    private var image: ByteArray? = null

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var storage: FirebaseStorage

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var name: Any

    private lateinit var img: Any

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater)
        setLayout()
        adaptLayout()
        setNavigation()
        openGallery()
        updateProfile()
        return binding.root
    }

    private fun setLayout() {
        val userRef = firestore.collection("users")
        val query = userRef.whereEqualTo("userId", firebaseAuth.currentUser?.uid)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    name = document.getString("name") ?: ""
                    img = document.getString("img") ?: ""
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

    private fun updateProfile() {
        binding.txtSave.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val newName = binding.edtName.text.toString()
            val storageRef = storage.reference
            val userRef = firestore.collection("users")


            image?.let { img ->
                val uuidImg = UUID.randomUUID()
                val imageRef = storageRef.child("profiles/$uuidImg.jpg")
                val uploadImg = imageRef.putBytes(img)

                try {
                    var imgUri = ""
                    uploadImg.addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl
                            .addOnSuccessListener { uri ->
                                imgUri = uri.toString()

                                updateUserData(newName, imgUri)
                            }
                    }
                } catch (e: Exception) {
                    handleError(e)
                }
            } ?: run {
                updateUserData(newName, img.toString())
            }
        }
    }

    private fun updateUserData(newName: Any, img: String) {
        val userRef = firestore.collection("users")
        val query = userRef.whereEqualTo("userId", firebaseAuth.currentUser?.uid)
        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val user = hashMapOf(
                        "img" to img,
                        "name" to newName,
                        "userId" to firebaseAuth.currentUser?.uid
                    )
                    document.reference.update(user)
                        .addOnSuccessListener {
                            binding.progressBar.visibility = View.GONE
                            findNavController().popBackStack()
                        }
                        .addOnFailureListener { e ->
                            handleError(e)
                        }
                }
            }
            .addOnFailureListener { e ->
                handleError(e)
            }
    }

    private fun handleError(e: Exception) {
        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        binding.progressBar.visibility = View.GONE
    }

    private fun openGallery() {
        binding.viewUpdate.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setType("image/*")
            startActivityForResult(intent, 1)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 1 && resultCode == Activity.RESULT_OK) {
            binding.imgProfile.setImageURI(data?.data)
            val selectedImageUri: Uri? = data?.data
            val bitmap: Bitmap? = selectedImageUri?.let { uri ->
                try {
                    val inputStream = requireActivity().contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(inputStream)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            bitmap?.let { selectedBitmap ->
                val baos = ByteArrayOutputStream()
                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val byteArray: ByteArray = baos.toByteArray()
                image = byteArray

            }

        }
    }

    private fun adaptLayout() {
        binding.btnLogout.visibility = View.GONE
        binding.navBack.visibility = View.VISIBLE
        binding.editIcon.visibility = View.VISIBLE
        binding.edtName.requestFocus()
        binding.edtName.isFocusableInTouchMode = true
        binding.txtSave.text = "Save"
    }

    private fun setNavigation() {
        binding.navBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


}