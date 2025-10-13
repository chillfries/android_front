package com.example.myapplication.presentation.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.RecipeIngredient
import com.example.myapplication.databinding.ItemRecipeIngredientBinding

class RecipeIngredientAdapter : ListAdapter<RecipeIngredient, RecipeIngredientAdapter.RecipeIngredientViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeIngredientViewHolder {
        val binding = ItemRecipeIngredientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipeIngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeIngredientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeIngredientViewHolder(private val binding: ItemRecipeIngredientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ingredient: RecipeIngredient) {
            binding.apply {
                // 레이아웃 ID: text_ingredient_name, text_quantity
                textIngredientName.text = ingredient.name
                textQuantity.text = ingredient.quantity

                // 재료 보유 여부에 따라 색상 변경 (isSatisfied 필드 사용)
                val colorResId = if (ingredient.isSatisfied) {
                    R.color.color_text_dark
                } else {
                    R.color.color_text_medium
                }

                val color = ContextCompat.getColor(root.context, colorResId)
                textIngredientName.setTextColor(color)
                textQuantity.setTextColor(color)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RecipeIngredient>() {
        override fun areItemsTheSame(oldItem: RecipeIngredient, newItem: RecipeIngredient): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: RecipeIngredient, newItem: RecipeIngredient): Boolean {
            return oldItem == newItem
        }
    }
}