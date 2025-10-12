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
        // ⭐ 해결: 어댑터 생성 시, ViewModel에서 allStorages 목록을 가져와 전달합니다.
        listAdapter = IngredientListAdapter(
            allStorages = viewModel.storages.value ?: emptyList(),
            onDeleteClick = { ingredient ->
                viewModel.deleteIngredient(ingredient.id)
                updatedIngredients.remove(ingredient.id)
            },
            onIngredientUpdate = { updatedIngredient ->
                updatedIngredients[updatedIngredient.id] = updatedIngredient
            },
            onExpiryDateClick = { ingredient, onDateSelected ->
                showDatePicker(ingredient.expiryDate, onDateSelected)
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
            val currentList = ingredientsInStorage.map {
                updatedIngredients[it.id] ?: it
            }
            listAdapter.submitList(currentList)
        }
    }

    private fun showDatePicker(initialDate: Date, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance().apply { time = initialDate }
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                onDateSelected(newCalendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}