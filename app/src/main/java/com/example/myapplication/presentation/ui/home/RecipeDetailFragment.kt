package com.example.myapplication.presentation.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.model.RecipeDetail
// import com.example.myapplication.data.model.RecipeIngredientDetail // 사용하지 않으므로 제거
import com.example.myapplication.databinding.FragmentRecipeDetailBinding
import com.example.myapplication.presentation.base.BaseFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken // ⭐ TypeToken import 추가
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipeDetailFragment : BaseFragment<FragmentRecipeDetailBinding>(FragmentRecipeDetailBinding::inflate) {

    private val viewModel: RecipeDetailViewModel by viewModels()
    // args는 RecipeDetailFragmentArgs로 가정합니다.
    private val args: RecipeDetailFragmentArgs by navArgs()
    private lateinit var ingredientAdapter: RecipeIngredientAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()

        // 1. ViewModel에 레시피 ID 목록 전달
        viewModel.loadRecipes(args.recipeIds.toList())

        // 2. 데이터 관찰 및 UI 업데이트
        observeViewModel()
    }

    private fun setupRecyclerView() {
        ingredientAdapter = RecipeIngredientAdapter()
        binding.recyclerIngredients.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ingredientAdapter
        }
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
        // 북마크 버튼 리스너는 observeViewModel에서 동적으로 설정됩니다.
    }

    private fun observeViewModel() {
        viewModel.recipeDetails.observe(viewLifecycleOwner) { result ->
            result.onSuccess { recipeList ->
                val recipeEntity = recipeList.firstOrNull()
                if (recipeEntity != null) {
                    try {
                        // JSON String을 RecipeDetail 객체로 변환
                        val recipeDetail = Gson().fromJson(recipeEntity.json, RecipeDetail::class.java)
                        updateUI(recipeDetail, recipeEntity.bookmark)

                        // 북마크 리스너 설정
                        binding.buttonBookmark.setOnClickListener {
                            viewModel.toggleBookmark(recipeEntity.id)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "레시피 데이터 파싱 오류: ${e.message}", Toast.LENGTH_LONG).show()
                        // 파싱 실패 시 돌아가도록 합니다.
                        findNavController().popBackStack()
                    }
                } else {
                    Toast.makeText(requireContext(), "레시피 상세 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }.onFailure {
                Toast.makeText(requireContext(), "레시피 로드 실패: ${it.message}", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun updateUI(detail: RecipeDetail, isBookmarked: Boolean) {
        // 1. 썸네일 이미지 (Glide 사용)
        Glide.with(this)
            .load(detail.thumbnailUrl)
            .placeholder(R.drawable.ic_food_placeholder) //             .into(binding.imageThumbnail)

        // 2. 요리명
        binding.textDishName.text = detail.title ?: "제목 없음"

        // 3. 메타 정보 (UI/UX 개선: 순서 조정 및 포맷)
        val difficulty = when (detail.difficulty) {
            1 -> "하"
            2 -> "중"
            3 -> "상"
            else -> "-"
        }
        // 🚨 서버 DTO에 cuisine_type이 없으므로, title에서 유추하거나 일단 제외합니다.
        // 현재 DTO (RecipeDetail)에 cuisine_type이 없기 때문에 메타 정보에서 제외하거나,
        // Dish 정보를 불러오는 추가 API 호출이 필요합니다. 일단 DTO에 있는 정보만 사용합니다.
        val metaInfo = "${detail.servingSize ?: "-"} | 난이도: $difficulty | ${detail.cookingTime ?: "-"}분"
        binding.textMetaInfo.text = metaInfo

        // 4. 필요 재료
        ingredientAdapter.submitList(detail.ingredients)

        // 5. 레시피 순서 (JSONB -> List<String> 파싱을 가정)
        val instructionsType = object : TypeToken<List<String>>() {}.type
        val instructionsList: List<String> = try {
            Gson().fromJson<List<String>>(detail.instructions, instructionsType)
        } catch (e: Exception) {
            // 파싱 실패 시, 단일 문자열로 간주하여 목록에 넣습니다. (서버 API 변경 권장)
            listOf("순서 정보 파싱 실패: ${detail.instructions.take(50)}...")
        }

        binding.textInstructions.text = instructionsList.filter { it.isNotBlank() }.mapIndexed { index, step ->
            "${index + 1}. $step"
        }.joinToString("\n")


        // 6. 유튜브 링크
        if (detail.youtubeUrl.isNullOrBlank()) {
            binding.buttonYoutube.visibility = View.GONE
        } else {
            binding.buttonYoutube.visibility = View.VISIBLE
            binding.buttonYoutube.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(detail.youtubeUrl)))
            }
        }

        // 7. 북마크 아이콘 상태
        binding.buttonBookmark.setImageResource(
            if (isBookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_outline // ic_bookmark 사용
        )
        // 색상 업데이트 (Task 5: UI/UX) - 북마크 시 강조
        binding.buttonBookmark.setColorFilter(ContextCompat.getColor(requireContext(), if (isBookmarked) R.color.dday_imminent_orange else R.color.white))
    }
}