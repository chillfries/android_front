// src/main/java/com/example/myapplication/feature/fridge/IngredientIconAdapter.kt

package com.example.myapplication.feature.fridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemIngredientIconBinding
import com.example.myapplication.feature.fridge.data.Ingredient
import java.time.LocalDate
import java.time.ZoneId // ⭐ 추가 ⭐
import java.time.temporal.ChronoUnit
import java.util.Date // ⭐ 추가 ⭐

/**
 * 저장 공간 박스 내부에 들어가는 재료 아이콘 슬라이더를 위한 어댑터
 */
class IngredientIconAdapter(
    private var ingredients: List<Ingredient>,
    private val onIngredientClick: (Ingredient) -> Unit
) : RecyclerView.Adapter<IngredientIconAdapter.IngredientIconViewHolder>() {

    class IngredientIconViewHolder(
        val binding: ItemIngredientIconBinding
    ) : RecyclerView.ViewHolder(binding.root)

    // ⭐ Date to LocalDate 확장 함수 (D-Day 계산용) ⭐
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

        // 1. 재료 아이콘 설정 (임시 플레이스홀더 사용)
        val iconResId = context.resources.getIdentifier(
            "ic_food_placeholder",
            "drawable",
            context.packageName
        )
        holder.binding.imageIngredientIcon.setImageResource(if (iconResId != 0) iconResId else R.drawable.ic_add)

        // 2. D-Day 텍스트 설정 (소비기한 계산)
        val today = LocalDate.now()
        // ⭐ Date 객체를 LocalDate로 변환하여 사용 ⭐
        val expiryDate = ingredient.expiryDate.toLocalDate()

        val daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate).toInt()

        holder.binding.textDDay.visibility = View.VISIBLE

        when {
            daysUntilExpiry < 0 -> {
                holder.binding.textDDay.text = "D+${daysUntilExpiry * -1}"
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

        // 3. 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            onIngredientClick(ingredient)
        }
    }

    // ⭐ 데이터 갱신 함수 ⭐
    fun updateIngredients(newIngredients: List<Ingredient>) {
        ingredients = newIngredients
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = ingredients.size
}