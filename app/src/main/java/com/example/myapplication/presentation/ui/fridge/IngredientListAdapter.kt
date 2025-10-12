package com.example.myapplication.presentation.ui.fridge

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemIngredientEditBinding
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IngredientListAdapter(
    private val allStorages: List<Storage>,
    private val onDeleteClick: (Ingredient) -> Unit,
    private val onIngredientUpdate: (Ingredient) -> Unit,
    private val onExpiryDateClick: (Ingredient, (Date) -> Unit) -> Unit
) : ListAdapter<Ingredient, IngredientListAdapter.IngredientViewHolder>(IngredientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemIngredientEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IngredientViewHolder(private val binding: ItemIngredientEditBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var nameWatcher: TextWatcher? = null
        private var quantityWatcher: TextWatcher? = null

        fun bind(ingredient: Ingredient) {
            // 리스너를 null로 설정하여 무한 루프 방지
            nameWatcher?.let { binding.etIngredientName.removeTextChangedListener(it) }
            quantityWatcher?.let { binding.etIngredientQuantity.removeTextChangedListener(it) }
            binding.autoCompleteStorage.onItemClickListener = null
            binding.etIngredientExpiryDate.setOnClickListener(null)
            binding.autoCompleteStorage.setOnClickListener(null) // ⭐ 리스너 초기화 추가

            // 데이터 초기 설정
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.etIngredientName.setText(ingredient.name)
            binding.etIngredientQuantity.setText(ingredient.quantity.toString())
            binding.etIngredientExpiryDate.setText(sdf.format(ingredient.expiryDate))

            // 저장 공간 드롭다운 설정
            val storageNames = allStorages.map { it.name }
            val adapter = ArrayAdapter(itemView.context, R.layout.dropdown_menu_item, storageNames)
            binding.autoCompleteStorage.setAdapter(adapter)
            binding.autoCompleteStorage.setText(ingredient.storageLocation, false)

            // 이벤트 리스너 다시 설정
            binding.btnDelete.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(bindingAdapterPosition))
                }
            }

            nameWatcher = binding.etIngredientName.doOnTextChanged { text, _, _, _ ->
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onIngredientUpdate(getItem(bindingAdapterPosition).copy(name = text.toString()))
                }
            }
            quantityWatcher = binding.etIngredientQuantity.doOnTextChanged { text, _, _, _ ->
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onIngredientUpdate(getItem(bindingAdapterPosition).copy(quantity = text.toString().toIntOrNull() ?: 0))
                }
            }
            binding.autoCompleteStorage.setOnItemClickListener { _, _, position, _ ->
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onIngredientUpdate(getItem(bindingAdapterPosition).copy(storageLocation = storageNames[position]))
                }
            }

            // ⭐ 해결: 저장 공간 클릭 시 드롭다운 메뉴가 나타나도록 리스너를 추가합니다.
            binding.autoCompleteStorage.setOnClickListener {
                binding.autoCompleteStorage.showDropDown()
            }

            binding.etIngredientExpiryDate.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onExpiryDateClick(getItem(bindingAdapterPosition)) { newDate ->
                        binding.etIngredientExpiryDate.setText(sdf.format(newDate))
                        if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                            onIngredientUpdate(getItem(bindingAdapterPosition).copy(expiryDate = newDate))
                        }
                    }
                }
            }
        }
    }

    private class IngredientDiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem == newItem
        }
    }
}