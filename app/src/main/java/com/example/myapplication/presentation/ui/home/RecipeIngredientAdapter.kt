package com.example.myapplication.presentation.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.model.RecipeIngredientDetail
import com.example.myapplication.databinding.ItemRecipeIngredientBinding

class RecipeIngredientAdapter : ListAdapter<RecipeIngredientDetail, RecipeIngredientAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRecipeIngredientBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ingredientDetail: RecipeIngredientDetail) {
            binding.textIngredientName.text = ingredientDetail.ingredient.name
            binding.textQuantity.text = ingredientDetail.quantityDisplay ?: "적당량"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RecipeIngredientDetail>() {
        override fun areItemsTheSame(oldItem: RecipeIngredientDetail, newItem: RecipeIngredientDetail): Boolean {
            return oldItem.ingredient.name == newItem.ingredient.name
        }

        override fun areContentsTheSame(oldItem: RecipeIngredientDetail, newItem: RecipeIngredientDetail): Boolean {
            return oldItem == newItem
        }
    }
}