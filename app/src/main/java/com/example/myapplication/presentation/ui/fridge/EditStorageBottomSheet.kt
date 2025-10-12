package com.example.myapplication.presentation.ui.fridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.BottomSheetEditStorageBinding
import com.example.myapplication.databinding.DialogSelectStorageIconBinding
import com.example.myapplication.domain.model.Storage
import com.example.myapplication.presentation.model.StorageIconData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditStorageBottomSheet : BottomSheetDialogFragment() {

    private val fridgeViewModel: FridgeViewModel by activityViewModels()
    private var _binding: BottomSheetEditStorageBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentStorage: Storage
    private var newIconResName: String = ""
    private var newName: String = ""

    companion object {
        private const val ARG_STORAGE = "storage_object"
        fun newInstance(storage: Storage): EditStorageBottomSheet {
            return EditStorageBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_STORAGE, storage)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEditStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Argument로부터 Storage 객체 가져오기
        arguments?.getParcelable<Storage>(ARG_STORAGE)?.let {
            currentStorage = it
            newName = it.name
            newIconResName = it.iconResName
        } ?: run {
            Toast.makeText(requireContext(), "오류: 저장 공간 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        // 2. 초기 UI 설정
        binding.editTextStorageName.setText(newName)
        updateSelectedIcon(newIconResName)
        updateSaveButtonState()

        // 3. 리스너 설정
        binding.buttonSelectIcon.setOnClickListener { showIconSelectDialog() }
        binding.editTextStorageName.doOnTextChanged { text, _, _, _ ->
            newName = text.toString().trim()
            updateSaveButtonState()
        }
        binding.buttonSave.setOnClickListener { saveChanges() }
        binding.buttonDelete.setOnClickListener { showDeleteConfirmDialog() }
        binding.buttonClose.setOnClickListener { dismiss() }
    }


    private fun updateSelectedIcon(resName: String) {
        newIconResName = resName
        val iconResId = StorageIconData.getDrawableIdByName(requireContext(), resName)
        if (iconResId != 0) {
            binding.buttonSelectIcon.setImageResource(iconResId)
        }
        updateSaveButtonState()
    }

    private fun showIconSelectDialog() {
        val context = requireContext()
        val dialogBinding = DialogSelectStorageIconBinding.inflate(LayoutInflater.from(context))
        var iconSelectDialog: AlertDialog? = null

        val adapter = StorageIconAdapter(StorageIconData.iconList) { selectedIcon ->
            updateSelectedIcon(selectedIcon.resName)
            iconSelectDialog?.dismiss()
        }

        dialogBinding.recyclerIconGrid.apply {
            layoutManager = GridLayoutManager(context, 4)
            this.adapter = adapter
        }

        iconSelectDialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()
        iconSelectDialog.show()
    }

    private fun updateSaveButtonState() {
        val nameChanged = newName != currentStorage.name
        val iconChanged = newIconResName != currentStorage.iconResName
        binding.buttonSave.isEnabled = newName.isNotBlank() && (nameChanged || iconChanged)
    }

    private fun saveChanges() {
        if (newName.isBlank()) {
            Toast.makeText(requireContext(), "저장 공간 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        fridgeViewModel.updateStorage(currentStorage.id, newName, newIconResName)
        Toast.makeText(requireContext(), "'$newName'(으)로 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("저장 공간 삭제")
            .setMessage("'${currentStorage.name}'을(를) 정말 삭제하시겠습니까? 속해있던 재료는 '정리되지 않은 재료'로 이동됩니다.")
            .setPositiveButton("삭제") { _, _ ->
                fridgeViewModel.deleteStorage(currentStorage.id)
                Toast.makeText(requireContext(), "'${currentStorage.name}'이(가) 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}