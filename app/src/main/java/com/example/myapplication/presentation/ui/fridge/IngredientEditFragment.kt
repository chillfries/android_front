package com.example.myapplication.presentation.ui.fridge

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentIngredientEditBinding
import com.example.myapplication.databinding.FragmentIngredientItemBinding
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class IngredientEditFragment :
    BaseFragment<FragmentIngredientEditBinding>(FragmentIngredientEditBinding::inflate) {

    private val fridgeViewModel: FridgeViewModel by activityViewModels()
    private val ingredientEditViewModel: IngredientEditViewModel by activityViewModels()

    private val ingredientFormBindings = mutableListOf<FragmentIngredientItemBinding>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                findNavController().navigate(R.id.action_ingredientEditFragment_to_cameraFragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    "카메라 권한이 거부되었습니다. 재료를 수동으로 입력해 주세요.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 뒤로가기 버튼 처리 로직 복원
        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                ingredientEditViewModel.clearAllForms()
                findNavController().popBackStack()
                remove()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        restoreFormsFromViewModel()
        setupClickListeners()
        validateForm()

        setFragmentResultListener("camera_result") { _, bundle ->
            val recognized = bundle.getStringArrayList("recognized_ingredients")
            if (!recognized.isNullOrEmpty()) {
                val firstEmptyForm =
                    ingredientFormBindings.firstOrNull { it.editTextItemName.text.isNullOrBlank() }
                if (firstEmptyForm != null) {
                    firstEmptyForm.editTextItemName.setText(recognized.first())
                    recognized.drop(1).forEach { addIngredientForm(Ingredient(name = it, quantity = 1, unit = "개", storageLocation = "", expiryDate = Date())) }
                } else {
                    recognized.forEach { addIngredientForm(Ingredient(name = it, quantity = 1, unit = "개", storageLocation = "", expiryDate = Date())) }
                }
            }
        }

        fridgeViewModel.ingredientToEdit.observe(viewLifecycleOwner) { ingredient ->
            ingredient?.let {
                addIngredientForm(it)
                fridgeViewModel.clearIngredientToEdit()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        ingredientEditViewModel.saveManualIngredients(getCurrentFormIngredients())
    }

    private fun restoreFormsFromViewModel() {
        binding.ingredientFormContainer.removeAllViews() // 컨테이너 초기화
        ingredientFormBindings.clear()

        ingredientEditViewModel.manualIngredients.value?.forEach { addIngredientForm(it) }
        if (ingredientFormBindings.isEmpty()) {
            addIngredientForm()
        }
    }

    private fun setupClickListeners() {
        // ✅ ID: fab_scan_ingredient
        binding.fabScanIngredient.setOnClickListener {
            checkCameraPermissionAndRequest()
        }

        // ✅ ID: button_add_ingredient_text
        binding.buttonAddIngredientText.setOnClickListener {
            addIngredientForm()
        }

        // ✅ ID: button_register_all
        binding.buttonRegisterAll.setOnClickListener {
            val ingredientsToSave = getCurrentFormIngredients()
            if (ingredientsToSave.isNotEmpty()) {
                fridgeViewModel.addIngredients(ingredientsToSave)
                Toast.makeText(requireContext(), "재료가 추가되었습니다.", Toast.LENGTH_SHORT).show()
            }
            ingredientEditViewModel.clearAllForms()
            findNavController().popBackStack()
        }

        // ❗️ 'imageButtonBack' ID는 현재 레이아웃에 없으므로 삭제하고, 위의 backPressedCallback으로 대체합니다.
    }

    private fun addIngredientForm(initialIngredient: Ingredient? = null) {
        val itemBinding = FragmentIngredientItemBinding.inflate(
            LayoutInflater.from(context),
            binding.ingredientFormContainer, // ✅ ID: ingredient_form_container
            false
        )
        ingredientFormBindings.add(itemBinding)

        // '재료 추가' 버튼을 항상 맨 아래에 두기 위한 로직
        val container = binding.ingredientFormContainer
        val addButton = binding.buttonAddIngredientText
        container.removeView(addButton) // 버튼을 잠시 뗐다가
        container.addView(itemBinding.root) // 새 재료 폼을 추가하고
        container.addView(addButton) // 다시 버튼을 붙입니다.


        val storageNames = fridgeViewModel.storages.value?.map { it.name } ?: emptyList()
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, storageNames)
        itemBinding.autoCompleteStorage.setAdapter(adapter)

        initialIngredient?.let {
            itemBinding.editTextItemName.setText(it.name)
            itemBinding.editTextItemQuantity.setText(it.quantity.toString())
            itemBinding.autoCompleteStorage.setText(it.storageLocation, false)
            itemBinding.editTextItemExpiryDate.setText(
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(it.expiryDate)
            )
        }

        itemBinding.editTextItemName.doOnTextChanged { _, _, _, _ ->
            validateForm()
        }

        itemBinding.editTextItemExpiryDate.setOnClickListener {
            showDatePickerDialog(itemBinding)
        }

        itemBinding.buttonRemove.setOnClickListener {
            binding.ingredientFormContainer.removeView(itemBinding.root)
            ingredientFormBindings.remove(itemBinding)
            validateForm()
        }

        validateForm()
    }

    private fun showDatePickerDialog(itemBinding: FragmentIngredientItemBinding) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate =
                    String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth)
                itemBinding.editTextItemExpiryDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun getCurrentFormIngredients(): List<Ingredient> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return ingredientFormBindings.mapNotNull { itemBinding ->
            val name = itemBinding.editTextItemName.text.toString().trim()
            if (name.isBlank()) return@mapNotNull null

            val quantityString =
                itemBinding.editTextItemQuantity.text.toString().trim().ifEmpty { "1" }
            val expiryDateString = itemBinding.editTextItemExpiryDate.text.toString().trim()
            val quantityInt = quantityString.toIntOrNull() ?: 1
            val unitString = "개"

            val expiryDate: Date = try {
                sdf.parse(expiryDateString) ?: Date()
            } catch (e: Exception) {
                Date()
            }

            Ingredient(
                id = 0L,
                name = name,
                quantity = quantityInt,
                unit = unitString,
                expiryDate = expiryDate,
                storageLocation = itemBinding.autoCompleteStorage.text.toString().trim().ifEmpty { "정리되지 않은 재료" }
            )
        }
    }

    private fun checkCameraPermissionAndRequest() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                findNavController().navigate(R.id.action_ingredientEditFragment_to_cameraFragment)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun validateForm() {
        val isAtLeastOneItemValid = ingredientFormBindings.any {
            it.editTextItemName.text.toString().isNotBlank()
        }
        binding.buttonRegisterAll.isEnabled = isAtLeastOneItemValid
    }
}