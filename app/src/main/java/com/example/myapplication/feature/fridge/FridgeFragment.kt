package com.example.myapplication.feature.fridge // ⭐ 패키지 경로 변경 ⭐

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // ViewModel 사용을 위해 필수
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R // R 파일은 그대로 사용
import com.example.myapplication.databinding.FragmentFridgeBinding
import android.view.LayoutInflater // ⭐ 추가 ⭐
import android.view.ViewGroup   // ⭐ 추가 ⭐
import android.util.Log // ⭐ (선택적) Log를 사용한다면 추가 ⭐
import com.example.myapplication.feature.fridge.data.Ingredient
import com.example.myapplication.feature.fridge.data.Storage // Storage 모델 추가
import android.widget.TextView // 툴팁 표시를 위한 텍스트 뷰 import (item_storage_box에 정의됨)
import com.example.myapplication.MainActivity // ⭐ 이 줄을 추가합니다. ⭐

// 이 클래스는 com.example.myapplication.FridgeFragment가 아닌 feature.fridge.FridgeFragment가 됩니다.

class FridgeFragment : Fragment(R.layout.fragment_fridge) {

    // ⭐ 1. ViewModel 주입: 이제 FridgeViewModel을 사용합니다. ⭐
    private val viewModel: FridgeViewModel by viewModels()

    private var _binding: FragmentFridgeBinding? = null
    private val binding get() = _binding!!

    // ⭐ 새로 추가: StorageBoxAdapter 인스턴스 ⭐
    private lateinit var storageBoxAdapter: StorageBoxAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFridgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 어댑터 초기화 및 RecyclerView 설정
        storageBoxAdapter = StorageBoxAdapter(
            storages = emptyList(),
            ingredientsByStorage = emptyMap(),
            onIngredientClick = ::handleIngredientIconClick,
            // ⭐ 1단계: handleStorageSettingsClick 참조로 변경 ⭐
            onStorageSettingsClick = ::handleStorageSettingsClick
        )

        binding.recyclerStorageBoxes.adapter = storageBoxAdapter

        // 2. LiveData 관찰 및 데이터 갱신
        observeViewModel()

        // ⭐ 새로 추가: 편집할 재료 데이터 관찰 및 네비게이션 ⭐
        viewModel.ingredientToEdit.observe(viewLifecycleOwner) { ingredient ->
            if (ingredient != null) {
                // IngredientEditFragment로 이동
                findNavController().navigate(R.id.action_fridgeFragment_to_ingredientEditFragment)
                // 이동했으므로 ViewModel 상태 초기화
                viewModel.clearIngredientToEdit()
            }
        }

        // ⭐ FAB 버튼 리스너 연결 (기존 showAddMenu 함수 호출) ⭐
        binding.fabAddMenu.setOnClickListener { // ID 변경: fab_add_ingredient -> fab_add_menu
            showAddMenu(it)
        }

        // TODO: Adapter 초기화 로직 추가 예정
    }

    // ⭐ 새로 추가: ViewModel LiveData 관찰 함수 ⭐
    private fun observeViewModel() {
        // ViewModel의 저장 공간 목록과 재료 목록을 관찰합니다.
        viewModel.storages.observe(viewLifecycleOwner) { storages ->
            viewModel.ingredients.value?.let { ingredients ->
                // 저장 공간 데이터가 변경되면, 재료 데이터와 결합하여 어댑터 갱신
                updateStorageBoxes(storages, ingredients)
            }
        }

        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            viewModel.storages.value?.let { storages ->
                // 재료 데이터가 변경되면, 저장 공간 데이터와 결합하여 어댑터 갱신
                updateStorageBoxes(storages, ingredients)
            }
        }
    }

    // ⭐ 재료를 저장 공간 이름으로 그룹화하는 로직은 이제 오류 없이 동작합니다. ⭐
    private fun updateStorageBoxes(storages: List<Storage>, ingredients: List<Ingredient>) {
        // 재료 목록을 저장 공간 ID별로 그룹화
        val ingredientsByStorage = ingredients.groupBy { ingredient ->
            // ingredient.storageLocation (String)으로 Storage를 찾고, 해당 Storage의 ID(Long)를 사용
            storages.find { it.name == ingredient.storageLocation }?.id ?: 1L // '정리되지 않은 재료' ID를 1L로 가정
        }

        storageBoxAdapter.updateData(storages, ingredientsByStorage)
    }

    // ⭐ 수정: 재료 아이콘 클릭 시 처리 로직 ⭐
    private fun handleIngredientIconClick(ingredient: Ingredient) {
        // 1. ViewModel에 편집할 재료 설정
        viewModel.selectIngredientForEdit(ingredient)

        // 2. Fragment의 LiveData 옵저버가 네비게이션을 실행합니다.

        // 이전의 툴팁 관련 코드는 삭제하거나 주석 처리합니다.
        // Toast.makeText(
        //     requireContext(),
        //     "${ingredient.name} 편집 모드로 이동",
        //     Toast.LENGTH_SHORT
        // ).show()
    }

    private fun showAddMenu(view: View) {
        val context = context ?: return
        val popup = PopupMenu(context, view)

        popup.menuInflater.inflate(R.menu.menu_fridge_add, popup.menu) // R.menu.menu_fridge_add 파일이 있다고 가정

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_add_storage -> {
                    // ⭐ 저장 공간 추가 선택 시 Bottom Sheet 띄우기 ⭐
                    val bottomSheet = AddStorageBottomSheet()
                    bottomSheet.show(childFragmentManager, bottomSheet.tag)
                    true
                }
                R.id.menu_add_ingredient -> {
                    // 재료 추가 페이지로 이동 (기존 로직 유지)
                    try {
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

    // ⭐ 새로 추가: 저장 공간 설정 버튼 클릭 시 처리 로직 ⭐
    private fun handleStorageSettingsClick(storage: Storage) {
        if (storage.isDefault) {
            Toast.makeText(
                requireContext(),
                "${storage.name}은 편집/삭제할 수 없습니다.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 2. 편집/삭제 Bottom Sheet Dialog 띄우기
        val bottomSheet = EditStorageBottomSheet.newInstance(storage)
        // childFragmentManager를 사용하여 Fragment 내에서 BottomSheet을 관리
        bottomSheet.show(childFragmentManager, bottomSheet.tag)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}