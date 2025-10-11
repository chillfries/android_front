package com.example.myapplication.presentation.ui.fridge

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentIngredientListEditBinding
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class IngredientListEditFragment : BaseFragment<FragmentIngredientListEditBinding>(FragmentIngredientListEditBinding::inflate) {

    private val viewModel: FridgeViewModel by activityViewModels()
    private val args: IngredientListEditFragmentArgs by navArgs()
    private lateinit var listAdapter: IngredientListAdapter

    // 수정된 재료들을 임시 저장할 맵 (Key: 재료 ID, Value: 수정된 Ingredient 객체)
    private val updatedIngredients = mutableMapOf<Long, Ingredient>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val storageId = args.storageId
        val storage = viewModel.storages.value?.find { it.id == storageId }
        if (storage == null) {
            Toast.makeText(requireContext(), "오류: 저장 공간을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        binding.tvStorageName.text = "'${storage.name}' 재료 편집"

        setupRecyclerView()
        observeViewModel(storage.name)

        binding.buttonComplete.setOnClickListener {
            if (updatedIngredients.isNotEmpty()) {
                viewModel.updateIngredients(updatedIngredients.values.toList())
                Toast.makeText(requireContext(), "'${storage.name}'의 재료가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        listAdapter = IngredientListAdapter(
            allStorages = viewModel.storages.value ?: emptyList(),
            onDeleteClick = { ingredient ->
                viewModel.deleteIngredient(ingredient.id)
                updatedIngredients.remove(ingredient.id)
            },
            onIngredientUpdate = { updatedIngredient ->
                updatedIngredients[updatedIngredient.id] = updatedIngredient
            },
            // 날짜 선택 클릭 시 DatePickerDialog를 보여주는 람다 전달
            onExpiryDateClick = { _, onDateSelected ->
                showDatePicker(onDateSelected)
            }
        )
        binding.rvIngredientList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }
    }

    private fun observeViewModel(storageName: String) {
        viewModel.ingredients.observe(viewLifecycleOwner) { allIngredients ->
            val ingredientsInStorage = allIngredients.filter { it.storageLocation == storageName }
            // submitList를 호출하기 전에 수정 맵에 있는 내용으로 최신 데이터를 반영
            val currentList = ingredientsInStorage.map {
                updatedIngredients[it.id] ?: it
            }
            listAdapter.submitList(currentList)
        }
    }

    // DatePickerDialog를 생성하고 보여주는 함수
    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val newCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            onDateSelected(newCalendar.time)
        }, year, month, day).show()
    }
}