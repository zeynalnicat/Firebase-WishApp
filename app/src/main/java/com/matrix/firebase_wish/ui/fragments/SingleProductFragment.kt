package com.matrix.firebase_wish.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.matrix.firebase_wish.databinding.FragmentSingleProductBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SingleProductFragment : Fragment() {
    private lateinit var binding: FragmentSingleProductBinding

    private var productId = ""

    @Inject
    lateinit var firestore: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSingleProductBinding.inflate(inflater)
        getProductId()
        setLayout()
        setNavigation()
        return binding.root
    }

    private fun getProductId() {
        arguments?.let {
            productId = it.getString("productId", "")
        }
    }

    private fun setLayout() {
        val productRef = firestore.collection("products")
        val query = productRef.whereEqualTo("id", productId)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val product = querySnapshot.documents[0]

                    product?.let { productDocumentSnapshot ->
                        Glide.with(binding.root)
                            .load(productDocumentSnapshot["imgUri"])
                            .into(binding.imgProduct)
                        binding.ratingBar.rating =
                            (productDocumentSnapshot["rating"] as? Number)?.toFloat() ?: 0.0F
                        binding.txtTitle.text = productDocumentSnapshot["title"] as? String ?: ""
                        binding.txtBrand.text = productDocumentSnapshot["brand"] as? String ?: ""
                        binding.txtDescription.text =
                            productDocumentSnapshot["description"] as? String ?: ""
                        binding.txtStock.text = productDocumentSnapshot["stocks"].toString()
                        binding.txtPrice.text = "$${productDocumentSnapshot["price"]}"
                    }
                } else {

                    Log.w("Firestore", "No product found for productId: $productId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching product details: ${exception.message}")
            }

    }

    private fun setNavigation() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


}