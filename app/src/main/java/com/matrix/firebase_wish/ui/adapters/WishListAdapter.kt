package com.matrix.firebase_wish.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matrix.firebase_wish.databinding.ItemWishListBinding
import com.matrix.firebase_wish.model.Product

class WishListAdapter : RecyclerView.Adapter<WishListAdapter.ViewHolder>() {

    private val diffCallBack = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

    }

    private val diffUtil = AsyncListDiffer(this, diffCallBack)




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemWishListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return diffUtil.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(diffUtil.currentList[position])
    }

    inner class ViewHolder(private val binding:ItemWishListBinding):RecyclerView.ViewHolder(binding.root){
         fun bind(current: Product){
             Glide.with(binding.root).load(current.images).into(binding.imgProduct)
             binding.txtCategory.text = current.category
             binding.txtDescription.text = current.description
             binding.txtPrice.text = "$${current.price}"
             binding.txtProductName.text = current.title
             binding.ratingBar2.rating = current.rating.toFloat()
         }
    }


    fun submitList(list:List<Product>){
        diffUtil.submitList(list)
    }

}