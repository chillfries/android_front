// src/main/java/com/example/myapplication/feature/fridge/IngredientEditFragment.kt

package com.example.myapplication.feature.fridge

import android.Manifest
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // Activity-scoped (FridgeViewModel)
import androidx.fragment.app.viewModels // Fragment-scoped (IngredientEditViewModel)
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentIngredientEditBinding
import com.example.myapplication.databinding.FragmentIngredientItemBinding
import com.example.myapplication.feature.fridge.data.Ingredient
import com.example.myapplication.feature.fridge.data.Storage
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.time.format.DateTimeParseException

class IngredientEditFragment : Fragment(R.layout.fragment_ingredient_edit) {

    // ⭐ 1. 폼 상태 관리 (Fragment-Scoped) ⭐
    private val editViewModel: IngredientEditViewModel by viewModels()

    // ⭐ 2. 데이터 저장 및 목록 관리 (Activity-Scoped) ⭐
    private val fridgeViewModel: FridgeViewModel by activityViewModels()

    private var _binding: FragmentIngredientEditBinding? = null
    private val binding get() = _binding!!

    // 동적 생성된 재료 입력 폼 목록
    private val ingredientFormBindings = mutableListOf<FragmentIngredientItemBinding>()

    // 현재 사용 가능한 저장 공간 목록
    private var availableStorages: List<Storage> = emptyList()

    // 권한 요청 콜백
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                navigateToCameraFragment()
            } else {
                Toast.makeText(requireContext(), "카메라 권한이 거부되었습니다.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIngredientEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ⭐ 0. 저장 공간 목록 초기화 및 관찰 ⭐
        fridgeViewModel.storages.observe(viewLifecycleOwner) { storages ->
            availableStorages = storages
            ingredientFormBindings.forEach { itemBinding ->
                setupStorageDropdown(itemBinding)
            }
        }

        // ... (중략: 폼 초기화 및 상태 복원 로직) ...
        val ingredientToEdit = fridgeViewModel.ingredientToEdit.value

        binding.ingredientFormContainer.removeAllViews()
        ingredientFormBindings.clear()

        if (ingredientToEdit != null) {
            // A) 편집 모드
            binding.titleText.text = "재료 편집"
            addIngredientForm(ingredientToEdit)
            binding.buttonAddIngredientText.visibility = View.GONE
        } else {
            // B) 추가 모드 (상태 복원 또는 신규 입력)
            binding.titleText.text = "재료 추가 및 편집"
            binding.buttonAddIngredientText.visibility = View.VISIBLE

            val manualList = editViewModel.manualIngredients.value
            val recognizedList = editViewModel.recognizedIngredients.value

            when {
                manualList?.isNotEmpty() == true -> {
                    manualList.forEach { addIngredientForm(it) }
                    editViewModel.clearManualIngredients()
                }
                recognizedList?.isNotEmpty() == true -> {
                    recognizedList.forEach { addIngredientForm(it) }
                    editViewModel.clearRecognizedIngredients()
                }
                else -> {
                    addIngredientForm()
                }
            }
        }

        // ⭐ 2. 버튼 리스너 연결 ⭐
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

    // ... (중략: addIngredientForm, removeIngredientForm, setupStorageDropdown 함수) ...
    private fun addIngredientForm(initialIngredient: Ingredient? = null) {
        val inflater = LayoutInflater.from(context)
        val itemBinding = FragmentIngredientItemBinding.inflate(inflater, binding.ingredientFormContainer, false)

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
                fridgeViewModel.clearIngredientToEdit()
                Toast.makeText(requireContext(), "재료 삭제 후 복귀 로직 (TODO)", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupStorageDropdown(itemBinding: FragmentIngredientItemBinding, initialLocation: String? = null) {
        val storageNames = availableStorages.map { it.name }.ifEmpty { listOf("정리되지 않은 재료") }
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, storageNames)
        val autoCompleteTextView = itemBinding.autoCompleteStorage

        autoCompleteTextView.setAdapter(adapter)

        val selectedStorageName = initialLocation ?: storageNames.first()
        autoCompleteTextView.setText(selectedStorageName, false)
        autoCompleteTextView.setOnClickListener { autoCompleteTextView.showDropDown() }
    }


    /**
     * ⭐ 수정/추가 로직: FridgeViewModel의 addIngredients와 saveIngredient를 사용합니다. ⭐
     */
    private fun registerAllIngredients() {
        val ingredientsToSave = getCurrentFormIngredients()

        if (ingredientsToSave.isNotEmpty()) {
            val ingredientToEdit = fridgeViewModel.ingredientToEdit.value

            if (ingredientToEdit != null) {
                // 1. 편집 모드: ID 유지하여 saveIngredient 호출
                val updatedIngredient = ingredientsToSave.first().copy(id = ingredientToEdit.id)
                fridgeViewModel.saveIngredient(updatedIngredient)
                Toast.makeText(requireContext(), "재료 '${updatedIngredient.name}'가(이) 수정되었습니다.", Toast.LENGTH_LONG).show()

            } else {
                // 2. 추가 모드: addIngredients 호출
                fridgeViewModel.addIngredients(ingredientsToSave)
                Toast.makeText(requireContext(), "총 ${ingredientsToSave.size}개의 재료가 냉장고에 등록되었습니다.", Toast.LENGTH_LONG).show()
            }

            editViewModel.clearIngredients()
            fridgeViewModel.clearIngredientToEdit()
            findNavController().popBackStack(R.id.fridgeFragment, false)
        } else {
            Toast.makeText(requireContext(), "등록할 유효한 재료가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * 재료 추출 로직: Date 변환 포함
     */
    private fun getCurrentFormIngredients(): List<Ingredient> {
        return ingredientFormBindings
            .mapNotNull { itemBinding ->
                val name = itemBinding.editTextItemName.text.toString().trim()
                if (name.isBlank()) return@mapNotNull null

                val quantityString = itemBinding.editTextItemQuantity.text.toString().trim().ifEmpty { "1" }
                val expiryDateString = itemBinding.editTextItemExpiryDate.text.toString().trim()

                val quantityInt = quantityString.toIntOrNull() ?: 1
                val unitString = "개"

                val expiryDate: Date = try {
                    LocalDate.parse(expiryDateString).toDate()
                } catch (e: DateTimeParseException) {
                    LocalDate.now().toDate()
                }

                // ID는 등록/수정 로직에서 0L 또는 기존 ID로 덮어쓰므로 0L로 둡니다.
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
            // do nothing
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                editText.setText(selectedDate.toString())
                updateRegisterButtonState()
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // ⭐ Date 변환 확장 함수 (필수) ⭐
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

    // ... (checkCameraPermissionAndRequest, navigateToCameraFragment, onDestroyView는 유지) ...
    private fun checkCameraPermissionAndRequest() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
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
            Toast.makeText(requireContext(), "ERROR: 카메라 페이지 이동 경로를 찾을 수 없습니다. (Nav Graph ID 오류)", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        ingredientFormBindings.clear()
    }
}