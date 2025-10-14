package com.example.myapplication.presentation.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.model.RecommendedDish
import com.example.myapplication.databinding.FragmentRecipeDetailBinding
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipeDetailFragment : BaseFragment<FragmentRecipeDetailBinding>(FragmentRecipeDetailBinding::inflate) {

    // NavArgs를 사용하여 RecommendedDish 객체를 받습니다.
    private val args: RecipeDetailFragmentArgs by navArgs()

    private lateinit var ingredientAdapter: RecipeIngredientAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dish = args.recommendedDish

        setupViews()
        bindRecipeDetails(dish)
    }

    private fun setupViews() {
        // 뒤로가기 버튼 설정 (레이아웃 ID: button_back)
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 재료 목록 RecyclerView 설정
        ingredientAdapter = RecipeIngredientAdapter()
        binding.recyclerIngredients.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ingredientAdapter
        }
    }

    private fun bindRecipeDetails(dish: RecommendedDish) {
        binding.apply {
            // 1. 이미지 (Glide 사용 - build.gradle.kts에 Glide 의존성이 추가되어 있어야 함)
            Glide.with(this.root.context)
                .load(dish.youtubeThumbnailUrl)
                .placeholder(R.drawable.ic_food_placeholder)
                .error(R.drawable.ic_food_placeholder)
                .centerCrop()
                .into(imageThumbnail)

            // 2. 요리명
            textDishName.text = dish.dishName

            // 3. 메타 정보 (카테고리 표시)
            textMetaInfo.text = dish.category

            // 4. 필요 재료 (어댑터에 데이터 제출)
            ingredientAdapter.submitList(dish.requiredIngredients)

            // 5. 레시피 순서
            textInstructions.text = dish.recipeSteps.joinToString("\n\n") {
                "${it.step}. ${it.description}"
            }

            // 6. 유튜브 링크 버튼
            if (dish.youtubeUrl.isNotBlank()) {
                buttonYoutube.visibility = View.VISIBLE
                buttonYoutube.setOnClickListener {
                    openYoutubeLink(dish.youtubeUrl)
                }
            } else {
                buttonYoutube.visibility = View.GONE
            }
        }
    }

    private fun openYoutubeLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // 오류 처리
        }
    }
}