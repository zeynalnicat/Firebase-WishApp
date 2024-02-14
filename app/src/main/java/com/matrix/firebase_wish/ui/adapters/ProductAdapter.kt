package com.matrix.firebase_wish.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matrix.firebase_wish.R
import com.matrix.firebase_wish.databinding.ItemSingleProductBinding
import com.matrix.firebase_wish.model.Product

class ProductAdapter(
    private val nav: (Bundle) -> Unit,
    private val insertDb: (String)
    -> Unit
) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

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
        val view =
            ItemSingleProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return diffUtil.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(diffUtil.currentList[position])
    }

    inner class ViewHolder(private val binding: ItemSingleProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(current: Product) {
            Glide.with(binding.root).load(current.images).into(binding.imgProduct)
            binding.txtProductName.text = current.title
            binding.imgLike.setImageResource(if (current.isAdded) R.drawable.icon_heart_filled else R.drawable.icon_heart)
            binding.txtProductPrice.text = "$" + "${current.price}"
            binding.imgLike.setOnClickListener {
                current.isAdded = !current.isAdded
                insertDb(current.id)
                notifyItemChanged(layoutPosition)
            }
            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("productId", current.id)
                nav(bundle)
            }
        }
    }

    fun submitList(products: List<Product>) {
        diffUtil.submitList(products)
    }
}