package com.example.myapplication.presentation.ui.fridge

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentFridgeBinding
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

// ⭐ 해결 3: Hilt 진입점 어노테이션 추가
@AndroidEntryPoint
class FridgeFragment : BaseFragment<FragmentFridgeBinding>(FragmentFridgeBinding::inflate) {

    private val viewModel: FridgeViewModel by activityViewModels()
    private lateinit var storageBoxAdapter: StorageBoxAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storageBoxAdapter = StorageBoxAdapter(
            storages = emptyList(),
            ingredientsByStorage = emptyMap(),
            onIngredientClick = ::handleIngredientIconClick,
            onStorageSettingsClick = ::handleStorageSettingsClick
        )
        binding.recyclerStorageBoxes.adapter = storageBoxAdapter

        observeViewModel()

        viewModel.ingredientToEdit.observe(viewLifecycleOwner) { ingredient ->
            if (ingredient != null) {
                findNavController().navigate(R.id.action_fridgeFragment_to_ingredientEditFragment)
                // viewModel.clearIngredientToEdit() // 이동 후 onPause에서 처리되도록 변경
            }
        }

        binding.fabAddMenu.setOnClickListener {
            showAddMenu(it)
        }
    }

    private fun observeViewModel() {
        viewModel.storages.observe(viewLifecycleOwner) { storages ->
            viewModel.ingredients.value?.let { ingredients ->
                updateStorageBoxes(storages, ingredients)
            }
        }

        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            viewModel.storages.value?.let { storages ->
                updateStorageBoxes(storages, ingredients)
            }
        }
    }

    private fun updateStorageBoxes(storages: List<Storage>, ingredients: List<Ingredient>) {
        val ingredientsByStorage = ingredients.groupBy { ingredient ->
            storages.find { it.name == ingredient.storageLocation }?.id ?: 1L
        }
        storageBoxAdapter.updateData(storages, ingredientsByStorage)
    }

    private fun handleIngredientIconClick(ingredient: Ingredient) {
        viewModel.selectIngredientForEdit(ingredient)
    }

    private fun showAddMenu(view: View) {
        val context = context ?: return
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.menu_fridge_add, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_add_storage -> {
                    val bottomSheet = AddStorageBottomSheet()
                    bottomSheet.show(childFragmentManager, bottomSheet.tag)
                    true
                }
                R.id.menu_add_ingredient -> {
                    try {
                        viewModel.clearIngredientToEdit() // 새 재료 추가 모드로 진입하기 전에 편집 상태 초기화
                        findNavController().navigate(R.id.action_fridgeFragment_to_ingredientEditFragment)
                    } catch (e: Exception) {
                        Toast.makeText(context, "ERROR: Navigation 경로 오류", Toast.LENGTH_LONG).show()
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun handleStorageSettingsClick(storage: Storage) {
        if (storage.isDefault) {
            Toast.makeText(requireContext(), "${storage.name}은 편집/삭제할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val bottomSheet = EditStorageBottomSheet.newInstance(storage)
        bottomSheet.show(childFragmentManager, bottomSheet.tag)
    }
}