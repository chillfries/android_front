// 파일: main/java/com/example/myapplication/presentation/ui/home/HomeFragment.kt

package com.example.myapplication.presentation.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle // ✅ Lifecycle.State.STARTED 사용
import androidx.lifecycle.lifecycleScope // ✅ Fragment의 코루틴 스코프
import androidx.lifecycle.repeatOnLifecycle // ✅ 안전한 수집 함수
import com.example.myapplication.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch // ✅ 코루틴 빌더 (launch)

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchListener()
        observeViewModel()

        // 초기 로드: 재료 기반 레시피 추천 실행
        viewModel.searchRecipes(query = null)
    }

    private fun setupSearchListener() {
        // 키보드의 돋보기 버튼(Action Search) 리스너
        binding.etSearchQuery.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString().trim()
                if (query.isNotBlank()) {
                    viewModel.searchRecipes(query)
                } else {
                    // 키워드가 없으면 재료 기반 추천만 다시 실행
                    viewModel.searchRecipes(query = null)
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun observeViewModel() {
        // 1. 검색 결과 관찰 (LiveData)
        viewModel.searchResult.observe(viewLifecycleOwner) { result ->
            if (result.total > 0) {
                binding.tvRecommendationTitle.text = if (binding.etSearchQuery.text.isNullOrBlank()) {
                    "👨‍🍳 당신을 위한 레시피 추천 (${result.total}개)"
                } else {
                    "🔎 검색 결과 (${result.total}개)"
                }
                // TODO: 리사이클러뷰 어댑터에 결과 (result.results) 바인딩
            } else {
                binding.tvRecommendationTitle.text = "결과 없음"
                // TODO: 결과 없음을 표시하는 UI 로직
            }
        }

        // 2. 오류 메시지 관찰 (LiveData)
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }

        // 3. D-Day 대시보드용 재료 목록 관찰 (Flow)
        lifecycleScope.launch { // Fragment의 생명주기에 맞게 코루틴 시작
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { // STARTED 상태에서만 수집
                viewModel.allIngredientsFlow.collect { ingredients -> // Flow 수집
                    val daysInMillis = 3L * 24 * 60 * 60 * 1000L
                    val imminentCount = ingredients.count { it.expiryDate.time <= System.currentTimeMillis() + daysInMillis }

                    if (imminentCount > 0) {
                        binding.tvDdayTitle.text = "🚨 유통기한 임박 재료 ($imminentCount 개)"
                        binding.tvDdayTitle.visibility = View.VISIBLE
                    } else {
                        binding.tvDdayTitle.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}