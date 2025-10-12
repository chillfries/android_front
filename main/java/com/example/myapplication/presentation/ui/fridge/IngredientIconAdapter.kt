package com.example.myapplication.presentation.ui.fridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemIngredientIconBinding
// ⭐ 수정: domain.model 패키지의 클래스를 import 합니다. ⭐
import com.example.myapplication.domain.model.Ingredient
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

class IngredientIconAdapter(
    private var ingredients: List<Ingredient>,
    private val onIngredientClick: (Ingredient) -> Unit
) : RecyclerView.Adapter<IngredientIconAdapter.IngredientIconViewHolder>() {

    class IngredientIconViewHolder(
        val binding: ItemIngredientIconBinding
    ) : RecyclerView.ViewHolder(binding.root)

    private fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientIconViewHolder {
        val binding = ItemIngredientIconBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IngredientIconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientIconViewHolder, position: Int) {
        val ingredient = ingredients[position]
        val context = holder.itemView.context

        val iconResId = context.resources.getIdentifier(
            "ic_food_placeholder",
            "drawable",
            context.packageName
        )
        holder.binding.imageIngredientIcon.setImageResource(if (iconResId != 0) iconResId else R.drawable.ic_add)

        val today = LocalDate.now()
        val expiryDate = ingredient.expiryDate.toLocalDate()

        val daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate).toInt()

        holder.binding.textDDay.visibility = View.VISIBLE

        when {
            daysUntilExpiry < 0 -> {
                holder.binding.textDDay.text = "D+${-daysUntilExpiry}"
                holder.binding.textDDay.setBackgroundResource(R.drawable.bg_dday_expired)
            }
            daysUntilExpiry <= 3 -> {
                holder.binding.textDDay.text = "D-$daysUntilExpiry"
                holder.binding.textDDay.setBackgroundResource(R.drawable.bg_dday_imminent)
            }
            else -> {
                holder.binding.textDDay.text = "D-$daysUntilExpiry"
                holder.binding.textDDay.setBackgroundResource(R.drawable.bg_dday_normal)
            }
        }

        holder.itemView.setOnClickListener {
            onIngredientClick(ingredient)
        }
    }

    fun updateIngredients(newIngredients: List<Ingredient>) {
        this.ingredients = newIngredients
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = ingredients.size
}