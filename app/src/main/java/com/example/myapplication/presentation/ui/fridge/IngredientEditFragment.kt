package com.example.myapplication.presentation.ui.fridge

import android.Manifest
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentIngredientEditBinding
import com.example.myapplication.databinding.FragmentIngredientItemBinding
import com.example.myapplication.domain.model.Ingredient
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class IngredientEditFragment :
    BaseFragment<FragmentIngredientEditBinding>(FragmentIngredientEditBinding::inflate) {

    private val editViewModel: IngredientEditViewModel by viewModels()
    private val fridgeViewModel: FridgeViewModel by activityViewModels()

    private val ingredientFormBindings = mutableListOf<FragmentIngredientItemBinding>()
    private var availableStorages: List<Storage> = emptyList()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                navigateToCameraFragment()
            } else {
                Toast.makeText(
                    requireContext(),
                    "카메라 권한이 거부되었습니다.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fridgeViewModel.storages.observe(viewLifecycleOwner) { storages ->
            availableStorages = storages
            ingredientFormBindings.forEach { itemBinding ->
                setupStorageDropdown(itemBinding)
            }
        }

        val ingredientToEdit = fridgeViewModel.ingredientToEdit.value

        binding.ingredientFormContainer.removeAllViews()
        ingredientFormBindings.clear()

        if (ingredientToEdit != null) {
            binding.titleText.text = "재료 편집"
            addIngredientForm(ingredientToEdit)
            binding.buttonAddIngredientText.visibility = View.GONE
        } else {
            binding.titleText.text = "재료 추가"
            binding.buttonAddIngredientText.visibility = View.VISIBLE

            val manualList = editViewModel.manualIngredients.value
            val recognizedList = editViewModel.recognizedIngredients.value

            when {
                !manualList.isNullOrEmpty() -> {
                    manualList.forEach { addIngredientForm(it) }
                    editViewModel.clearManualIngredients()
                }
                !recognizedList.isNullOrEmpty() -> {
                    recognizedList.forEach { addIngredientForm(it) }
                    editViewModel.clearRecognizedIngredients()
                }
                else -> {
                    addIngredientForm()
                }
            }
        }

        binding.buttonAddIngredientText.setOnClickListener {
            addIngredientForm()
        }
        binding.fabScanIngredient.setOnClickListener {
            checkCameraPermissionAndRequest()
        }
        binding.buttonRegisterAll.setOnClickListener {
            registerAllIngredients()
        }

        updateRegisterButtonState()
    }

    private fun addIngredientForm(initialIngredient: Ingredient? = null) {
        val inflater = LayoutInflater.from(context)
        val itemBinding =
            FragmentIngredientItemBinding.inflate(inflater, binding.ingredientFormContainer, false)

        ingredientFormBindings.add(itemBinding)

        val container = binding.ingredientFormContainer
        val addButton = binding.buttonAddIngredientText

        container.removeView(addButton)
        container.addView(itemBinding.root)
        container.addView(addButton)

        if (initialIngredient != null) {
            itemBinding.editTextItemName.setText(initialIngredient.name)
            itemBinding.editTextItemQuantity.setText(initialIngredient.quantity.toString())
            itemBinding.editTextItemExpiryDate.setText(initialIngredient.expiryDate.toLocalDate().toString())
        } else {
            itemBinding.editTextItemExpiryDate.setText(LocalDate.now().toString())
            itemBinding.inputLayoutName.requestFocus()
        }

        itemBinding.editTextItemName.doOnTextChanged { _, _, _, _ -> updateRegisterButtonState() }
        itemBinding.editTextItemExpiryDate.setOnClickListener { showDatePicker(itemBinding) }
        itemBinding.editTextItemExpiryDate.doOnTextChanged { _, _, _, _ -> updateRegisterButtonState() }
        itemBinding.buttonRemove.setOnClickListener { removeIngredientForm(itemBinding) }

        setupStorageDropdown(itemBinding, initialIngredient?.storageLocation)
        updateRegisterButtonState()
    }

    private fun removeIngredientForm(itemBinding: FragmentIngredientItemBinding) {
        binding.ingredientFormContainer.removeView(itemBinding.root)
        ingredientFormBindings.remove(itemBinding)
        updateRegisterButtonState()

        if (ingredientFormBindings.isEmpty()) {
            if (fridgeViewModel.ingredientToEdit.value == null) {
                addIngredientForm()
            } else {
                fridgeViewModel.deleteIngredient(fridgeViewModel.ingredientToEdit.value!!.id)
                fridgeViewModel.clearIngredientToEdit()
                Toast.makeText(requireContext(), "재료가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupStorageDropdown(
        itemBinding: FragmentIngredientItemBinding,
        initialLocation: String? = null
    ) {
        val storageNames = availableStorages.map { it.name }.ifEmpty { listOf("정리되지 않은 재료") }
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, storageNames)
        val autoCompleteTextView = itemBinding.autoCompleteStorage

        autoCompleteTextView.setAdapter(adapter)

        val selectedStorageName = initialLocation ?: storageNames.first()
        autoCompleteTextView.setText(selectedStorageName, false)
        autoCompleteTextView.setOnClickListener { autoCompleteTextView.showDropDown() }
    }

    private fun registerAllIngredients() {
        val ingredientsToSave = getCurrentFormIngredients()

        if (ingredientsToSave.isNotEmpty()) {
            val ingredientToEdit = fridgeViewModel.ingredientToEdit.value

            if (ingredientToEdit != null) {
                val updatedIngredient = ingredientsToSave.first().copy(id = ingredientToEdit.id)
                fridgeViewModel.saveIngredient(updatedIngredient)
                Toast.makeText(
                    requireContext(),
                    "재료 '${updatedIngredient.name}'가(이) 수정되었습니다.",
                    Toast.LENGTH_LONG
                ).show()

            } else {
                fridgeViewModel.addIngredients(ingredientsToSave)
                Toast.makeText(
                    requireContext(),
                    "총 ${ingredientsToSave.size}개의 재료가 냉장고에 등록되었습니다.",
                    Toast.LENGTH_LONG
                ).show()
            }

            editViewModel.clearIngredients()
            fridgeViewModel.clearIngredientToEdit()
            findNavController().popBackStack(R.id.fridgeFragment, false)
        } else {
            Toast.makeText(requireContext(), "등록할 유효한 재료가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentFormIngredients(): List<Ingredient> {
        return ingredientFormBindings
            .mapNotNull { itemBinding ->
                val name = itemBinding.editTextItemName.text.toString().trim()
                if (name.isBlank()) return@mapNotNull null

                val quantityString =
                    itemBinding.editTextItemQuantity.text.toString().trim().ifEmpty { "1" }
                val expiryDateString = itemBinding.editTextItemExpiryDate.text.toString().trim()

                val quantityInt = quantityString.toIntOrNull() ?: 1
                val unitString = "개"

                val expiryDate: Date = try {
                    LocalDate.parse(expiryDateString).toDate()
                } catch (e: DateTimeParseException) {
                    LocalDate.now().toDate()
                }

                Ingredient(
                    id = 0L,
                    name = name,
                    quantity = quantityInt,
                    unit = unitString,
                    expiryDate = expiryDate,
                    storageLocation = itemBinding.autoCompleteStorage.text.toString().trim()
                )
            }
    }

    private fun updateRegisterButtonState() {
        val hasAnyValidName = ingredientFormBindings.any {
            it.editTextItemName.text.toString().trim().isNotBlank()
        }
        binding.buttonRegisterAll.isEnabled = hasAnyValidName
    }

    private fun showDatePicker(itemBinding: FragmentIngredientItemBinding) {
        val calendar = Calendar.getInstance()
        val editText = itemBinding.editTextItemExpiryDate

        try {
            val date = LocalDate.parse(editText.text.toString())
            calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
        } catch (e: Exception) {
            // 날짜 파싱 실패 시 현재 날짜 사용
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                editText.setText(selectedDate.toString())
                updateRegisterButtonState()
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(
                Calendar.DAY_OF_MONTH
            )
        )
        datePickerDialog.show()
    }

    private fun LocalDate.toDate(): Date {
        return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    private fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    override fun onPause() {
        super.onPause()
        if (fridgeViewModel.ingredientToEdit.value == null && ingredientFormBindings.isNotEmpty()) {
            val currentIngredients = getCurrentFormIngredients()
            editViewModel.saveManualIngredients(currentIngredients)
        }
        fridgeViewModel.clearIngredientToEdit()
    }

    private fun checkCameraPermissionAndRequest() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                navigateToCameraFragment()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun navigateToCameraFragment() {
        try {
            findNavController().navigate(R.id.action_ingredientEditFragment_to_cameraFragment)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "ERROR: 카메라 페이지 이동 경로를 찾을 수 없습니다.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // BaseFragment에서 _binding = null 처리를 해주므로, 여기서는 추가 작업만 수행
        ingredientFormBindings.clear()
    }
}