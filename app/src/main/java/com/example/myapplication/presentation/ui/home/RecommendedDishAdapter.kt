package com.example.myapplication.presentation.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.model.RecommendedDish
import com.example.myapplication.databinding.ItemRecipeCardBinding

class RecommendedDishAdapter(
    private val onItemClick: (RecommendedDish) -> Unit
) : ListAdapter<RecommendedDish, RecommendedDishAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRecipeCardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(bindingAdapterPosition))
                }
            }
        }

        fun bind(dish: RecommendedDish) {
            binding.textDishName.text = dish.dishName
            // ⭐ Task 5: 텍스트 변경 - 레시피 개수만 표시
            binding.textRecipeCount.text = "${dish.recipeIds.size}개의 레시피"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RecommendedDish>() {
        override fun areItemsTheSame(oldItem: RecommendedDish, newItem: RecommendedDish): Boolean {
            return oldItem.dishId == newItem.dishId
        }

        override fun areContentsTheSame(oldItem: RecommendedDish, newItem: RecommendedDish): Boolean {
            return oldItem == newItem
        }
    }
}