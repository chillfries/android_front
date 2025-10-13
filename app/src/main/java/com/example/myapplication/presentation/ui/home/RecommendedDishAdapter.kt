package com.example.myapplication.presentation.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
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
            val context = binding.root.context
            binding.textDishName.text = dish.dishName

            // ⭐ 수정: 'N개의 레시피' 대신 'N개의 재료 충족' 텍스트 사용
            binding.textRecipeCount.text = context.getString(
                R.string.satisfied_ingredient_count_format,
                dish.satisfiedIngredientCount
            )
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