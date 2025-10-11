package com.example.myapplication.presentation.ui.fridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemStorageBoxBinding
// ⭐ 수정: domain.model 패키지의 클래스를 import 합니다. ⭐
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage

class StorageBoxAdapter(
    private var storages: List<Storage>,
    private var ingredientsByStorage: Map<Long, List<Ingredient>>,
    private val onIngredientClick: (Ingredient) -> Unit,
    private val onStorageSettingsClick: (Storage) -> Unit
) : RecyclerView.Adapter<StorageBoxAdapter.StorageBoxViewHolder>() {

    private val ingredientAdapters = mutableMapOf<Long, IngredientIconAdapter>()

    class StorageBoxViewHolder(
        val binding: ItemStorageBoxBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageBoxViewHolder {
        val binding = ItemStorageBoxBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StorageBoxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StorageBoxViewHolder, position: Int) {
        val storage = storages[position]
        val context = holder.itemView.context

        holder.binding.textStorageName.text = storage.name

        val iconResId = context.resources.getIdentifier(
            storage.iconResName,
            "drawable",
            context.packageName
        )
        holder.binding.imageStorageIcon.setImageResource(if (iconResId != 0) iconResId else R.drawable.ic_fridge)

        if (storage.isDefault) {
            holder.binding.buttonStorageSettings.visibility = View.GONE
            holder.binding.buttonStorageSettings.setOnClickListener(null)
        } else {
            holder.binding.buttonStorageSettings.visibility = View.VISIBLE
            holder.binding.buttonStorageSettings.setOnClickListener {
                onStorageSettingsClick(storage)
            }
        }

        val currentIngredients = ingredientsByStorage[storage.id] ?: emptyList()

        if (ingredientAdapters[storage.id] == null) {
            val adapter = IngredientIconAdapter(currentIngredients, onIngredientClick)
            holder.binding.recyclerIngredientSlider.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = adapter
            }
            ingredientAdapters[storage.id] = adapter
        } else {
            ingredientAdapters[storage.id]?.updateIngredients(currentIngredients)
        }
    }

    override fun getItemCount(): Int = storages.size

    fun updateData(newStorages: List<Storage>, newIngredientsByStorage: Map<Long, List<Ingredient>>) {
        storages = newStorages
        ingredientsByStorage = newIngredientsByStorage
        notifyDataSetChanged()
    }
}