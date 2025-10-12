package com.example.myapplication.presentation.ui.fridge

import android.Manifest
import android.app.DatePickerDialog
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
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class IngredientEditFragment :
    BaseFragment<FragmentIngredientEditBinding>(FragmentIngredientEditBinding::inflate) {

    // ViewModel의 생존 범위를 Activity로 설정하여 데이터 유지
    private val editViewModel: IngredientEditViewModel by activityViewModels()
    private val fridgeViewModel: FridgeViewModel by activityViewModels()

    private val ingredientFormBindings = mutableListOf<FragmentIngredientItemBinding>()
    private var availableStorages: List<Storage> = emptyList()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                navigateToCameraFragment()
            } else {
                Toast.makeText(requireContext(), "카메라 권한이 거부되었습니다.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 뒤로 가기 버튼 처리: ViewModel 데이터 초기화 후 화면 종료
        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                editViewModel.clearAllIngredients()
                findNavController().popBackStack()
                remove() // 콜백 중복 호출 방지
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        // 2. 카메라 결과 처리: detach/attach 대신, 직접 재료 폼을 추가하는 방식으로 변경
        setFragmentResultListener("camera_result") { _, bundle ->
            val recognized = bundle.getStringArrayList("recognized_ingredients")
            if (!recognized.isNullOrEmpty()) {
                // 기존에 입력하던 내용이 비어있는 첫 번째 폼에 채워넣기
                val firstEmptyForm = ingredientFormBindings.firstOrNull { it.editTextItemName.text.isNullOrBlank() }
                if (firstEmptyForm != null) {
                    firstEmptyForm.editTextItemName.setText(recognized.first())
                    recognized.drop(1).forEach { addIngredientForm(Ingredient(name = it, quantity = 1, unit = "개", storageLocation = "", expiryDate = Date())) }
                } else {
                    recognized.forEach { addIngredientForm(Ingredient(name = it, quantity = 1, unit = "개", storageLocation = "", expiryDate = Date())) }
                }
            }
        }

        // 3. 사용 가능한 저장 공간 목록 관찰
        fridgeViewModel.storages.observe(viewLifecycleOwner) { storages ->
            availableStorages = storages
            ingredientFormBindings.forEach { itemBinding -> setupStorageDropdown(itemBinding) }
        }

        // 4. 화면이 다시 생성될 때 UI 복원
        binding.ingredientFormContainer.removeAllViews()
        ingredientFormBindings.clear()
        restoreFormsFromViewModel()

        // 5. 버튼 리스너 설정
        binding.buttonAddIngredientText.setOnClickListener { addIngredientForm() }
        binding.fabScanIngredient.setOnClickListener { checkCameraPermissionAndRequest() }
        binding.buttonRegisterAll.setOnClickListener {
            registerAllIngredients()
            editViewModel.clearAllIngredients()
            findNavController().popBackStack()
        }

        updateRegisterButtonState()
    }

    override fun onPause() {
        super.onPause()
        // 화면을 벗어날 때(카메라 이동 등) 현재 UI의 내용을 ViewModel에 저장
        val currentIngredients = getCurrentFormIngredients()
        if (currentIngredients.isNotEmpty()) {
            editViewModel.saveManualIngredients(currentIngredients)
        }
    }

    private fun restoreFormsFromViewModel() {
        val ingredientsToRestore = editViewModel.manualIngredients.value
        // 저장된 내용이 있으면 복원
        if (!ingredientsToRestore.isNullOrEmpty()) {
            ingredientsToRestore.forEach { addIngredientForm(it) }
        }
        // 만약 복원 후에도 폼이 하나도 없다면, 빈 폼 하나를 추가
        if (ingredientFormBindings.isEmpty()) {
            addIngredientForm()
        }
    }

    // --- 이하 코드는 UI 요소를 제어하는 함수들입니다 (수정 없음) ---

    private fun addIngredientForm(initialIngredient: Ingredient? = null) {
        val inflater = LayoutInflater.from(context)
        val itemBinding = FragmentIngredientItemBinding.inflate(inflater, binding.ingredientFormContainer, false)
        ingredientFormBindings.add(itemBinding)

        val container = binding.ingredientFormContainer
        val addButton = binding.buttonAddIngredientText
        container.removeView(addButton)
        container.addView(itemBinding.root)
        container.addView(addButton)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        if (initialIngredient != null) {
            itemBinding.editTextItemName.setText(initialIngredient.name)
            itemBinding.editTextItemQuantity.setText(if (initialIngredient.quantity > 0) initialIngredient.quantity.toString() else "1")
            itemBinding.editTextItemExpiryDate.setText(sdf.format(initialIngredient.expiryDate))
        } else {
            itemBinding.editTextItemExpiryDate.setText(sdf.format(Date()))
            itemBinding.inputLayoutName.requestFocus()
        }

        itemBinding.editTextItemName.doOnTextChanged { _, _, _, _ -> updateRegisterButtonState() }
        itemBinding.editTextItemExpiryDate.setOnClickListener { showDatePicker(itemBinding) }
        itemBinding.buttonRemove.setOnClickListener { removeIngredientForm(itemBinding) }

        setupStorageDropdown(itemBinding, initialIngredient?.storageLocation)
        updateRegisterButtonState()
    }

    private fun removeIngredientForm(itemBinding: FragmentIngredientItemBinding) {
        binding.ingredientFormContainer.removeView(itemBinding.root)
        ingredientFormBindings.remove(itemBinding)
        updateRegisterButtonState()
        if (ingredientFormBindings.isEmpty()) {
            addIngredientForm()
        }
    }

    private fun setupStorageDropdown(itemBinding: FragmentIngredientItemBinding, initialLocation: String? = null) {
        val storageNames = availableStorages.map { it.name }.ifEmpty { listOf("정리되지 않은 재료") }
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, storageNames)
        val autoCompleteTextView = itemBinding.autoCompleteStorage
        autoCompleteTextView.setAdapter(adapter)

        val selectedStorageName = if (initialLocation.isNullOrBlank() || !storageNames.contains(initialLocation)) storageNames.first() else initialLocation
        autoCompleteTextView.setText(selectedStorageName, false)
        autoCompleteTextView.setOnClickListener { autoCompleteTextView.showDropDown() }
    }

    private fun registerAllIngredients() {
        val ingredientsToSave = getCurrentFormIngredients()
        if (ingredientsToSave.isNotEmpty()) {
            fridgeViewModel.addIngredients(ingredientsToSave)
            Toast.makeText(requireContext(), "총 ${ingredientsToSave.size}개의 재료가 등록되었습니다.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "등록할 유효한 재료가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentFormIngredients(): List<Ingredient> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return ingredientFormBindings.mapNotNull { itemBinding ->
            val name = itemBinding.editTextItemName.text.toString().trim()
            if (name.isBlank()) return@mapNotNull null

            val quantityString = itemBinding.editTextItemQuantity.text.toString().trim().ifEmpty { "1" }
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
                storageLocation = itemBinding.autoCompleteStorage.text.toString().trim()
            )
        }
    }

    private fun updateRegisterButtonState() {
        binding.buttonRegisterAll.isEnabled = ingredientFormBindings.any {
            it.editTextItemName.text.toString().trim().isNotBlank()
        }
    }

    private fun showDatePicker(itemBinding: FragmentIngredientItemBinding) {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date = sdf.parse(itemBinding.editTextItemExpiryDate.text.toString())
            if (date != null) calendar.time = date
        } catch (e: Exception) { /* 파싱 실패 시 현재 날짜 사용 */ }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                itemBinding.editTextItemExpiryDate.setText(sdf.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

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
            Toast.makeText(requireContext(), "카메라 화면으로 이동할 수 없습니다.", Toast.LENGTH_LONG).show()
        }
    }
}