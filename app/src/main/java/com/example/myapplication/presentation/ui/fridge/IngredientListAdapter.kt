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
    // 날짜 선택 다이얼로그를 띄우기 위한 콜백 추가
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

        // 리스너들을 멤버 변수로 관리하여 제거/추가가 가능하도록 함
        private var nameWatcher: TextWatcher? = null
        private var quantityWatcher: TextWatcher? = null

        fun bind(ingredient: Ingredient) {
            // 데이터를 바인딩하기 전에 이전에 설정된 리스너들을 모두 제거하여 재활용 오류 방지
            nameWatcher?.let { binding.etIngredientName.removeTextChangedListener(it) }
            quantityWatcher?.let { binding.etIngredientQuantity.removeTextChangedListener(it) }
            binding.autoCompleteStorage.onItemClickListener = null
            binding.etIngredientExpiryDate.setOnClickListener(null)

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

            // 새로운 데이터에 맞는 리스너들을 다시 설정
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
            binding.etIngredientExpiryDate.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    // 클릭된 재료와, 날짜가 선택되었을 때 실행할 콜백을 Fragment로 전달
                    onExpiryDateClick(getItem(bindingAdapterPosition)) { newDate ->
                        // 날짜 선택이 완료되면 UI를 업데이트하고, 변경된 내용을 Fragment로 전달
                        binding.etIngredientExpiryDate.setText(sdf.format(newDate))
                        if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                            onIngredientUpdate(getItem(bindingAdapterPosition).copy(expiryDate = newDate))
                        }
                    }
                }
            }
        }
    }

    // DiffUtil 설정: RecyclerView가 어떤 아이템이 변경되었는지 효율적으로 감지하도록 함
    private class IngredientDiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem == newItem
        }
    }
}