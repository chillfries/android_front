// 파일: mmain/main/java/com/example/myapplication/presentation/ui/fridge/FridgeFragment.kt

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
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import com.example.myapplication.domain.model.Ingredient
@AndroidEntryPoint
class FridgeFragment : BaseFragment<FragmentFridgeBinding>(FragmentFridgeBinding::inflate) {

    private val viewModel: FridgeViewModel by activityViewModels()
    private lateinit var storageBoxAdapter: StorageBoxAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

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
        // ⭐ Task 2 이동: 유통기한 임박 재료 개수 관찰 및 표시
        viewModel.imminentExpiryIngredients.observe(viewLifecycleOwner) { ingredients ->
            val count = ingredients.size
            if (count > 0) {
                // 💡 FIX: ID 오류 방지를 위해 레이아웃에 맞춰 ID를 사용합니다.
                binding.tvImminentCount.text = "(${count}개)"
                binding.tvImminentCount.visibility = View.VISIBLE
                binding.dividerImminent.visibility = View.VISIBLE
                binding.tvImminentTitle.visibility = View.VISIBLE // 제목도 표시
            } else {
                binding.tvImminentCount.visibility = View.GONE
                binding.dividerImminent.visibility = View.GONE
                binding.tvImminentTitle.visibility = View.GONE // 제목도 숨김
            }

            // 레이아웃이 변경되었으므로 RecyclerView가 이 아래에 위치하도록 제약을 다시 걸어야 합니다.
            // 여기서는 코드에서 직접 제어를 하지 않고, 레이아웃 XML의 제약에 의존합니다.
        }

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
                // 이 액션은 nav_main.xml의 fridge_nav_graph 내에 정의되어 있어야 합니다.
                findNavController().navigate(R.id.action_fridgeFragment_to_ingredientEditFragment)
                viewModel.clearIngredientToEdit()
            }
        }
    }

    private fun updateStorageBoxes(storages: List<Storage>, ingredients: List<Ingredient>) {
        val ingredientsByStorage = ingredients.groupBy { ingredient ->
            // 재료의 storageLocation 이름과 일치하는 Storage 객체의 ID를 찾거나, 없으면 기본값(1L) 사용
            storages.find { it.name == ingredient.storageLocation }?.id ?:
            storages.firstOrNull { it.isDefault }?.id ?: 1L
        }
        // ✅ 어댑터에 데이터 업데이트 요청 (UI 표시)
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
                        viewModel.clearIngredientToEdit()
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