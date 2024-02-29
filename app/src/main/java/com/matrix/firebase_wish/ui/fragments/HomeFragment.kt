package com.matrix.firebase_wish.ui.fragments

import ConnectionLiveData
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.matrix.firebase_wish.R
import com.matrix.firebase_wish.databinding.FragmentHomeBinding
import com.matrix.firebase_wish.db.product.ProductDao
import com.matrix.firebase_wish.model.Product
import com.matrix.firebase_wish.resource.Resource
import com.matrix.firebase_wish.ui.MainActivity
import com.matrix.firebase_wish.ui.adapters.ProductAdapter
import com.matrix.firebase_wish.viewmodel.HomeViewModel
import com.matrix.firebase_wish.viewmodel.factories.HomeFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var productDao: ProductDao


    private val homeViewModel: HomeViewModel by viewModels {
        HomeFactory(
            firestore,
            firebaseAuth,
            productDao,

            )
    }


    private lateinit var connectionLiveData: ConnectionLiveData


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        bottomNavigation()
        setNavigation()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectionLiveData = ConnectionLiveData(requireContext().applicationContext)

        homeViewModel.checkDb()

        homeViewModel.products.observe(viewLifecycleOwner) { result ->
            handleProductsResult(result)
        }

        homeViewModel.categories.observe(viewLifecycleOwner) {
            handleCategoryResult(it)
        }

        connectionLiveData.observe(viewLifecycleOwner) { isConnected ->
            handleConnectionChange(isConnected)
        }

    }

    private fun handleProductsResult(result: Resource<List<Product>>) {
        when (result) {
            is Resource.Success -> {
                binding.progressBar.visibility = View.GONE
                homeViewModel.getCategories()
                setAdapter(result.data)
                homeViewModel.roomSize.observe(viewLifecycleOwner) {
                    if (it == 0 || it < result.data.size) {
                        homeViewModel.insertDB()
                    }
                }
            }

            is Resource.Error -> {
                Toast.makeText(requireContext(), result.exception.message, Toast.LENGTH_SHORT)
                    .show()
                binding.progressBar.visibility = View.GONE
            }

            else -> {
                binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun handleCategoryResult(result: Resource<Set<String>>) {
        when (result) {
            is Resource.Success -> {
                setChipGroup(result.data)
            }

            is Resource.Error -> {
                Toast.makeText(requireContext(), result.exception.message, Toast.LENGTH_LONG).show()
            }

            is Resource.Loading -> {
            }
        }
    }

    private fun handleConnectionChange(isConnected: Boolean) {
        if (isConnected) {
            homeViewModel.readData()
            setSearchBar()
            setSpinner(false)
            binding.imgWifi.visibility = View.GONE
            binding.swipeRefreshLayout.setOnRefreshListener {
                homeViewModel.readData()
                binding.swipeRefreshLayout.isRefreshing = false
            }

        } else {
            homeViewModel.getFromDB()
            setSearchBarRoom()
            setSpinner(true)
            binding.imgWifi.visibility = View.VISIBLE
            binding.swipeRefreshLayout.setOnRefreshListener {
                homeViewModel.getFromDB()
                binding.swipeRefreshLayout.isRefreshing = false
            }

        }
    }

    private fun setSearchBar() {
        binding.edtSearch.doAfterTextChanged {
            homeViewModel.search(it.toString())
        }
    }

    private fun setSearchBarRoom() {
        binding.edtSearch.doAfterTextChanged {
            homeViewModel.searchRoom(it.toString())
        }
    }

    private fun setChipGroup(categories: Set<String>) {
        val chipGroup = binding.chipGroup
        categories.forEach { category ->
            val chip = Chip(requireContext())
            chip.text = category
            chip.isClickable = true

            chip.setOnClickListener { view ->
                homeViewModel.getDbDueCategories(category)

            }

            chipGroup.addView(chip)

        }

    }


    private fun setAdapter(productList: List<Product>) {


        val adapter = ProductAdapter(
            {
                findNavController().navigate(
                    R.id.action_homeFragment_to_singleProductFragment,
                    it
                )
            },
            { product -> homeViewModel.insertWishList(product) },
            { product -> homeViewModel.insertCart(product) }
        )

        adapter.submitList(productList)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter
    }


    private fun bottomNavigation() {
        val activity = activity as MainActivity
        activity.setBottomNavigation(true)
    }

    private fun setNavigation() {
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addProductFragment)
        }
    }

    private fun setSpinner(isRoom: Boolean) {
        val list = arrayOf("Default", "A-Z", "Z-A", "ASC-Price", "DESC-Price")
        val spinner = binding.spinner
        val arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list)
        spinner.adapter = arrayAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                p1?.let {
                    (it as TextView).setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            android.R.color.white
                        )
                    )
                }

                homeViewModel.sort(list[p2], isRoom)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                homeViewModel.sort("Default", isRoom)
            }

        }
    }
}