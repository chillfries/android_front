package com.example.myapplication.presentation.ui.fridge

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentFridgeBinding
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FridgeFragment : BaseFragment<FragmentFridgeBinding>(FragmentFridgeBinding::inflate) {

    private val viewModel: FridgeViewModel by activityViewModels()
    private lateinit var storageBoxAdapter: StorageBoxAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // ⭐ 여기를 수정했습니다! loadData() -> loadStorages()
        viewModel.loadStorages()

        binding.fabAddMenu.setOnClickListener {
            showAddMenu(it)
        }
    }

    private fun setupRecyclerView() {
        storageBoxAdapter = StorageBoxAdapter(
            storages = emptyList(),
            ingredientsByStorage = emptyMap(),
            onIngredientClick = ::handleIngredientIconClick,
            onStorageBoxClick = ::handleStorageBoxClick,
            onStorageSettingsClick = ::handleStorageSettingsClick
        )
        binding.recyclerStorageBoxes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = storageBoxAdapter
        }
    }

    private fun observeViewModel() {
        // storages 데이터가 변경되면, ingredients 데이터와 함께 묶어서 어댑터에 전달
        viewModel.storages.observe(viewLifecycleOwner) { storages ->
            viewModel.ingredients.value?.let { ingredients ->
                updateStorageBoxes(storages, ingredients)
            }
        }

        // ingredients 데이터가 변경되면, storages 데이터와 함께 묶어서 어댑터에 전달
        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            viewModel.storages.value?.let { storages ->
                updateStorageBoxes(storages, ingredients)
            }
        }

        // 편집할 재료가 선택되면 IngredientEditFragment로 이동
        viewModel.ingredientToEdit.observe(viewLifecycleOwner) { ingredient ->
            if (ingredient != null) {
                findNavController().navigate(R.id.action_fridgeFragment_to_ingredientEditFragment)
                // 이동 후에는 선택 상태를 초기화하여 중복 이동을 방지합니다.
                viewModel.clearIngredientToEdit()
            }
        }
    }

    // Storage와 Ingredient 리스트를 받아서 어댑터가 사용할 데이터 형태로 가공하고 업데이트
    private fun updateStorageBoxes(storages: List<Storage>, ingredients: List<Ingredient>) {
        val ingredientsByStorage = ingredients.groupBy { ingredient ->
            // 재료의 저장 위치 이름과 일치하는 Storage 객체의 ID를 찾습니다. 없으면 기본값(1L) 사용
            storages.find { it.name == ingredient.storageLocation }?.id ?: 1L
        }
        storageBoxAdapter.updateData(storages, ingredientsByStorage)
    }

    private fun handleIngredientIconClick(ingredient: Ingredient) {
        Toast.makeText(requireContext(), "재료: ${ingredient.name}, 수량: ${ingredient.quantity}", Toast.LENGTH_SHORT).show()
    }

    private fun handleStorageBoxClick(storage: Storage) {
        val action = FridgeFragmentDirections.actionFridgeFragmentToIngredientListEditFragment(storage.id)
        findNavController().navigate(action)
    }

    private fun handleStorageSettingsClick(storage: Storage) {
        if (storage.isDefault) {
            Toast.makeText(requireContext(), "${storage.name}은 편집/삭제할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val bottomSheet = EditStorageBottomSheet.newInstance(storage)
        bottomSheet.show(childFragmentManager, bottomSheet.tag)
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
                        viewModel.clearIngredientToEdit() // 새 재료 추가 모드로 진입
                        findNavController().navigate(R.id.action_fridgeFragment_to_ingredientEditFragment)
                    } catch (e: Exception) {
                        Toast.makeText(context, "화면 이동에 실패했습니다.", Toast.LENGTH_LONG).show()
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}