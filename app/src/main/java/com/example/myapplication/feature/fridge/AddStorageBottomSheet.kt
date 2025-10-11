// src/main/java/com/example/myapplication/feature/fridge/AddStorageBottomSheet.kt

package com.example.myapplication.feature.fridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.BottomSheetAddStorageBinding // XML 바인딩 가정
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.DialogSelectStorageIconBinding // XML 바인딩 가정
import com.example.myapplication.R

/**
 * 새로운 저장 공간을 추가하기 위한 Bottom Sheet Dialog
 */
class AddStorageBottomSheet : BottomSheetDialogFragment() {

    // FridgeViewModel을 사용하여 데이터 저장소에 접근
    private val fridgeViewModel: FridgeViewModel by activityViewModels() // viewModel -> fridgeViewModel로 변경 (일관성을 위해)

    private var _binding: BottomSheetAddStorageBinding? = null
    private val binding get() = _binding!!

    // 현재 선택된 아이콘 리소스 이름 (초기값: 냉장고)
    private var selectedIconResName: String = StorageIconData.DEFAULT_ICON_RES_NAME

    // 아이콘 선택 Dialog 표시 함수 (이전 단계에서 작성된 내용 유지)
    private fun updateSelectedIcon(resName: String) {
        selectedIconResName = resName
        val iconResId = StorageIconData.getDrawableIdByName(requireContext(), resName)
        if (iconResId != 0) {
            binding.buttonSelectIcon.setImageResource(iconResId)
        }
    }

    private fun showIconSelectDialog() {
        val context = requireContext()
        val dialogBinding = DialogSelectStorageIconBinding.inflate(LayoutInflater.from(context))
        var iconSelectDialog: AlertDialog? = null // Dialog 참조를 위한 변수

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

    // ⭐ Bottom Sheet 생명주기 및 로직 연결 ⭐

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 초기 아이콘 설정
        updateSelectedIcon(selectedIconResName)

        // 2. 아이콘 선택 버튼 리스너
        binding.buttonSelectIcon.setOnClickListener {
            showIconSelectDialog()
        }

        // 3. 텍스트 변경 리스너 (저장 버튼 활성화/비활성화)
        binding.editTextStorageName.doOnTextChanged { text, _, _, _ ->
            // 이름이 비어있지 않을 때만 저장 버튼 활성화
            binding.buttonConfirm.isEnabled = text.toString().trim().isNotBlank()
        }

        // ⭐ 4. 저장 버튼 클릭 리스너 (핵심 로직) ⭐
        binding.buttonConfirm.setOnClickListener {
            val newName = binding.editTextStorageName.text.toString().trim()

            if (newName.isNotBlank()) {
                // ViewModel의 addStorage 함수 호출
                fridgeViewModel.addStorage(
                    newName,
                    selectedIconResName
                )
                Toast.makeText(requireContext(), "$newName 저장 공간이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                dismiss() // Bottom Sheet 닫기
            } else {
                Toast.makeText(requireContext(), "저장 공간 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. 닫기 버튼
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}