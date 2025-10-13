package com.example.myapplication.presentation.ui.home

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo // ⭐ import 추가
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var recipeAdapter: RecommendedDishAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners() // ⭐ 리스너 설정 함수 호출 추가
        observeViewModel()
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecommendedDishAdapter { selectedDish ->
            // ⭐ 수정: RecommendedDish 객체 전체를 인자로 전달하여 상세 화면으로 이동
            val action = HomeFragmentDirections.actionHomeFragmentToRecipeDetailFragment(
                recommendedDish = selectedDish
            )
            findNavController().navigate(action)
        }
        binding.rvRecipes.apply {
            adapter = recipeAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    // ⭐ Task 1, 4: 검색 및 필터 이벤트 리스너 추가
    private fun setupListeners() {
        // 1. 검색어 입력 리스너 (ENTER/Search 버튼)
        binding.etSearchQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // 2. 칩 그룹 필터 리스너 (재료 일치율)
        binding.chipGroupRatioFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedChip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds.first())
                val ratioText = checkedChip.text.toString().replace("%", "")
                val ratio = (ratioText.toIntOrNull() ?: 60) / 100f
                performSearch(ratio = ratio)
            }
        }
    }

    // ⭐ Task 4: 검색 수행 함수
    private fun performSearch(ratio: Float = getCurrentRatio()) {
        val query = binding.etSearchQuery.text?.toString()?.trim()
        viewModel.syncRecipes(query = query, ratio = ratio)
    }

    // ⭐ Task 4: 현재 선택된 칩의 비율을 가져오는 헬퍼 함수
    private fun getCurrentRatio(): Float {
        val checkedId = binding.chipGroupRatioFilter.checkedChipId
        // 기본값 60% (0.6f)
        return if (checkedId != View.NO_ID) {
            val checkedChip = binding.chipGroupRatioFilter.findViewById<com.google.android.material.chip.Chip>(checkedId)
            val ratioText = checkedChip.text.toString().replace("%", "")
            (ratioText.toIntOrNull() ?: 60) / 100f
        } else {
            0.6f
        }
    }

    private fun observeViewModel() {
        // ⭐ Task 1, 4: 표시될 레시피 목록 (dishesForDisplay) 관찰로 변경
        // 기존 recommendedDishes 대신 dishesForDisplay를 관찰합니다.
        viewModel.dishesForDisplay.observe(viewLifecycleOwner) { dishes ->
            if (dishes.isNotEmpty()) {
                // Task 5: UI/UX 개선 - 추천 목록에 개수 표시
                binding.tvRecommendationTitle.text = "나를 위한 레시피 🍳 (${dishes.size}개)"
                recipeAdapter.submitList(dishes)
            } else {
                binding.tvRecommendationTitle.text = "조건에 맞는 레시피가 없어요. 🤔"
                recipeAdapter.submitList(emptyList())
            }
        }
    }
}