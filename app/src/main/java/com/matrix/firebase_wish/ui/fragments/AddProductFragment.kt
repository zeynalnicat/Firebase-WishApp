package com.matrix.firebase_wish.ui.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.matrix.firebase_wish.databinding.FragmentAddProductBinding

import dagger.hilt.android.AndroidEntryPoint

import java.io.ByteArrayOutputStream
import java.util.UUID

import javax.inject.Inject


@AndroidEntryPoint
class AddProductFragment : Fragment() {
    private lateinit var binding: FragmentAddProductBinding

    private var image: ByteArray? = null
    private var rating: Double = 0.0

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var storage: FirebaseStorage


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddProductBinding.inflate(inflater)
        setNavigation()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRatingBar()
        selectImage()
        addProduct()
    }


    private fun selectImage() {
        binding.viewSelectImage.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)

        }

    }

    private fun addProduct() {
        binding.btnSubmit.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val category = binding.edtCategory.text.toString()
            val description = binding.edtDescription.text.toString()
            val brand = binding.edtBrand.text.toString()
            val price = if (binding.edtPrice.text.toString().isNotEmpty()) binding.edtPrice.text.toString().toInt() else 0
            val title = binding.edtTitle.text.toString()
            val stocks = if (binding.edtStocks.text.toString().isNotEmpty()) binding.edtStocks.text.toString().toInt() else 0

            val storageRef = storage.reference
            val uuidImg = UUID.randomUUID()
            val imageRef = storageRef.child("images/$uuidImg.jpg")

            image?.let { imageByteArray ->
                val uploadTask = imageRef.putBytes(imageByteArray)

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val imgUri = task.result.toString()

                        val uuid = UUID.randomUUID().toString()

                        val products = hashMapOf(
                            "description" to description,
                            "brand" to brand,
                            "category" to category,
                            "id" to uuid,
                            "imgUri" to imgUri,
                            "price" to price,
                            "rating" to rating,
                            "stocks" to stocks,
                            "title" to title
                        )

                        firestore.collection("products").add(products).addOnSuccessListener {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }.addOnFailureListener {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun setRatingBar() {
        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, fl, b ->
            rating = fl.toDouble()

        }

    }


    private fun setNavigation() {
        binding.navBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == 1) {
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
                    if (image != null) {
                        binding.iconStatus.setImageResource(com.matrix.firebase_wish.R.drawable.icon_success)
                        binding.iconStatus.visibility = View.VISIBLE
                        Log.d("ImageStatus", "Setting success icon")

                    } else {
                        binding.iconStatus.setImageResource(com.matrix.firebase_wish.R.drawable.icon_failed)
                        binding.iconStatus.visibility = View.VISIBLE
                    }
                }

            }
        }


}

