package com.matrix.firebase_wish.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.matrix.firebase_wish.databinding.FragmentCartBinding
import com.matrix.firebase_wish.model.Product
import com.matrix.firebase_wish.resource.Resource
import com.matrix.firebase_wish.ui.adapters.WishListAdapter
import com.matrix.firebase_wish.viewmodel.CartViewModel
import com.matrix.firebase_wish.viewmodel.factories.CartFactory

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    private val cartViewModel: CartViewModel by viewModels { CartFactory(firebaseAuth, firestore) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartViewModel.products.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.data.isNotEmpty()) {
                        binding.txtNothing.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        binding.txtQuantity.text = it.data.size.toString()
                        setAdapter(it.data)
                        calculatePrice(it.data)
                        binding.view.visibility = View.VISIBLE
                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.txtNothing.visibility = View.VISIBLE

                    }
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.view.visibility = View.GONE
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.view.visibility = View.GONE
                }

            }
        }


        cartViewModel.readData()
    }

    private fun setAdapter(data: List<Product>) {
        val adapter = WishListAdapter()
        adapter.submitList(data)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.recyclerView.adapter = adapter
    }


    private fun calculatePrice(data: List<Product>) {
        var total = 0
        data.forEach {
            total += it.price
        }
        binding.txtPrice.text = "$${total}"
    }


}