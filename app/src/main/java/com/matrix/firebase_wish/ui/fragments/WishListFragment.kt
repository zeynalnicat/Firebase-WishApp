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
import com.matrix.firebase_wish.databinding.FragmentWishListBinding
import com.matrix.firebase_wish.ui.adapters.WishListAdapter
import com.matrix.firebase_wish.viewmodel.WishListViewModel
import com.matrix.firebase_wish.viewmodel.factories.WishListFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WishListFragment : Fragment() {
    private lateinit var binding: FragmentWishListBinding

    @Inject
    lateinit var firebaseFirestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private val wishListViewModel: WishListViewModel by viewModels {
        WishListFactory(
            firebaseFirestore,
            firebaseAuth
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWishListBinding.inflate(inflater)



        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wishListViewModel.getProducts()
        setAdapter()


    }

    private fun setAdapter() {
        wishListViewModel.products.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    binding.txtNothing.visibility = View.GONE
                    val adapter = WishListAdapter()
                    adapter.submitList(it)
                    binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
                    binding.recyclerView.adapter = adapter
                } else {
                    binding.txtNothing.visibility = View.VISIBLE
                }

            }
        }
    }


}