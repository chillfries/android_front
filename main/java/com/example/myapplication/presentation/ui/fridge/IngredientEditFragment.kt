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
import androidx.navigation.fragment.navGraphViewModels // 수정된 import
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

    // ViewModel의 범위를 'fridge_nav_graph'로 변경
    private val editViewModel: IngredientEditViewModel by navGraphViewModels(R.id.fridge_nav_graph)
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

        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                editViewModel.clearAllIngredients()
                findNavController().popBackStack()
                // 콜백을 제거하여 중복 호출을 방지합니다.
                remove()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        setFragmentResultListener("camera_result") { _, bundle ->
            val recognized = bundle.getStringArrayList("recognized_ingredients")
            if (!recognized.isNullOrEmpty()) {
                editViewModel.addRecognizedIngredients(recognized)
                // 인식된 재료를 기반으로 폼을 추가합니다. (UI 자동 새로고침)
                recognized.forEach { ingredientName ->
                    val newIngredient = Ingredient(name = ingredientName, quantity = 1, unit = "개", storageLocation = availableStorages.firstOrNull()?.name ?: "", expiryDate = Date())
                    addIngredientForm(newIngredient)
                }
            }
        }

        fridgeViewModel.storages.observe(viewLifecycleOwner) { storages ->
            availableStorages = storages
            ingredientFormBindings.forEach { itemBinding -> setupStorageDropdown(itemBinding) }
        }

        binding.ingredientFormContainer.removeAllViews()
        ingredientFormBindings.clear()
        restoreFormsFromViewModel()

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
        // 화면을 벗어날 때 현재 폼의 내용을 ViewModel에 저장합니다.
        val currentIngredients = getCurrentFormIngredients()
        editViewModel.saveManualIngredients(currentIngredients)
    }

    private fun restoreFormsFromViewModel() {
        val manualList = editViewModel.manualIngredients.value
        if (!manualList.isNullOrEmpty()) {
            manualList.forEach { addIngredientForm(it) }
        }

        // ViewModel에 저장된 내용이 없을 경우에만 기본 폼을 추가합니다.
        if (binding.ingredientFormContainer.childCount <= 1) { // 1은 '추가하기' 버튼
            addIngredientForm()
        }
    }


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