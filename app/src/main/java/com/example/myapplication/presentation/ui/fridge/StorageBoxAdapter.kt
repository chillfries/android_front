package com.example.myapplication.presentation.ui.fridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemStorageBoxBinding
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage

class StorageBoxAdapter(
    private var storages: List<Storage>,
    private var ingredientsByStorage: Map<Long, List<Ingredient>>,
    private val onIngredientClick: (Ingredient) -> Unit,
    private val onStorageBoxClick: (Storage) -> Unit,
    private val onStorageSettingsClick: (Storage) -> Unit
) : RecyclerView.Adapter<StorageBoxAdapter.StorageBoxViewHolder>() {

    private val ingredientAdapters = mutableMapOf<Long, IngredientIconAdapter>()

    class StorageBoxViewHolder(val binding: ItemStorageBoxBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageBoxViewHolder {
        val binding = ItemStorageBoxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StorageBoxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StorageBoxViewHolder, position: Int) {
        val storage = storages[position]
        val context = holder.itemView.context
        val currentIngredients = ingredientsByStorage[storage.id] ?: emptyList()

        with(holder.binding) {
            textStorageName.text = storage.name
            // ⭐ 해결: ID를 textIngredientCount로 올바르게 참조
            textIngredientCount.text = context.getString(R.string.ingredient_count, currentIngredients.size)

            val iconResId = context.resources.getIdentifier(storage.iconResName, "drawable", context.packageName)
            imageStorageIcon.setImageResource(if (iconResId != 0) iconResId else R.drawable.ic_fridge)

            root.setOnClickListener { onStorageBoxClick(storage) }

            if (storage.isDefault) {
                buttonStorageSettings.visibility = View.GONE
                containerStorageBox.setBackgroundColor(ContextCompat.getColor(context, R.color.color_primary))
            } else {
                buttonStorageSettings.visibility = View.VISIBLE
                containerStorageBox.setBackgroundColor(ContextCompat.getColor(context, R.color.color_surface))
                buttonStorageSettings.setOnClickListener { onStorageSettingsClick(storage) }
            }

            if (currentIngredients.isEmpty()) {
                divider.visibility = View.GONE
                recyclerIngredientSlider.visibility = View.GONE
            } else {
                divider.visibility = View.VISIBLE
                recyclerIngredientSlider.visibility = View.VISIBLE
            }

            if (ingredientAdapters[storage.id] == null) {
                val adapter = IngredientIconAdapter(currentIngredients, onIngredientClick)
                recyclerIngredientSlider.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    this.adapter = adapter
                }
                ingredientAdapters[storage.id] = adapter
            } else {
                ingredientAdapters[storage.id]?.updateIngredients(currentIngredients)
            }
        }
    }

    override fun getItemCount(): Int = storages.size

    fun updateData(newStorages: List<Storage>, newIngredientsByStorage: Map<Long, List<Ingredient>>) {
        storages = newStorages
        ingredientsByStorage = newIngredientsByStorage
        notifyDataSetChanged()
    }
}