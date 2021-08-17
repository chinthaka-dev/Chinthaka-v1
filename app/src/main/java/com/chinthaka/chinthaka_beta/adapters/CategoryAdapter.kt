package com.chinthaka.chinthaka_beta.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.chinthaka.chinthaka_beta.R
import com.chinthaka.chinthaka_beta.data.entities.Category
import com.chinthaka.chinthaka_beta.data.entities.Comment
import com.chinthaka.chinthaka_beta.databinding.ItemCategoryBinding
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class CategoryAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(binding: ItemCategoryBinding): RecyclerView.ViewHolder(binding.root){
        val ivCategoryPicture: ImageView = binding.categoryImageView
        val ivSelection: ImageView = binding.selectionImageView
        val tvCategoryName: TextView = binding.categoryName
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Category>(){

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.categoryId == newItem.categoryId
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var categories: List<Category>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {

        val category = categories[position]
        holder.apply {
            glide.load(category.categoryImageUrl).into(ivCategoryPicture)

            //TBD
            //ivSelection.isVisible = category.userId == FirebaseAuth.getInstance().uid!!
            if(category.isSelected) {
                ivSelection.isVisible = true
                glide.load(R.drawable.bluetick).into(ivSelection)
            }
            else ivSelection.isVisible = false

            tvCategoryName.text = category.categoryName

            itemView.setOnClickListener{
                onViewClickListener?.let { click ->
                    click(category, position)
                }
            }
        }
    }

    private var onViewClickListener: ((Category, Int) -> Unit)? = null

    fun setOnViewClickListener(listener: (Category, Int) -> Unit){
        onViewClickListener = listener
    }
}