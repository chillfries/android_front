// src/main/java/com/example/myapplication/feature/fridge/EditStorageBottomSheet.kt (완성)

package com.example.myapplication.feature.fridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.BottomSheetEditStorageBinding // XML 바인딩 가정
import com.example.myapplication.feature.fridge.data.Storage
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.DialogSelectStorageIconBinding // XML 바인딩 가정
import com.example.myapplication.R // 리소스 접근용

/**
 * 기존 저장 공간의 이름/아이콘 편집 또는 삭제를 위한 Bottom Sheet Dialog
 */
class EditStorageBottomSheet : BottomSheetDialogFragment() {

    // Activity ViewModel 사용
    private val fridgeViewModel: FridgeViewModel by activityViewModels()

    private var _binding: BottomSheetEditStorageBinding? = null
    private val binding get() = _binding!!

    // Bundle에서 전달받은 현재 Storage 객체
    private lateinit var currentStorage: Storage
    // 사용자가 현재 선택한 새로운 아이콘 리소스 이름
    private var newIconResName: String = ""
    // 사용자가 입력한 새로운 이름
    private var newName: String = ""

    companion object {
        private const val ARG_STORAGE = "storage_object"

        // Storage 객체를 인수로 받는 인스턴스 생성 팩토리 메소드
        fun newInstance(storage: Storage): EditStorageBottomSheet {
            return EditStorageBottomSheet().apply {
                arguments = Bundle().apply {
                    // Storage 모델에 Parcelable이 구현되어 있어야 합니다.
                    putParcelable(ARG_STORAGE, storage)
                }
            }
        }
    }

    // ⭐ 1. 아이콘 선택 관련 함수들 (AddStorageBottomSheet와 유사) ⭐

    private fun updateSelectedIcon(resName: String) {
        newIconResName = resName
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
            iconSelectDialog?.dismiss() // 참조 변수를 사용하여 닫기
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


    // ⭐ 2. Bottom Sheet 기본 설정 ⭐

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bundle에서 Storage 객체 추출
        currentStorage = arguments?.getParcelable(ARG_STORAGE) ?: throw IllegalStateException("Storage argument is missing")

        // 초기값 설정: 현재 저장 공간의 이름과 아이콘을 복사
        newIconResName = currentStorage.iconResName
        newName = currentStorage.name
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

        // ⭐ 3. 초기 UI 설정 및 리스너 연결 ⭐

        // 초기 UI 설정: 기존 값으로 채우기
        binding.editTextStorageName.setText(currentStorage.name)
        updateSelectedIcon(currentStorage.iconResName)

        // 아이콘 선택 버튼 리스너
        binding.buttonSelectIcon.setOnClickListener {
            showIconSelectDialog()
        }

        // 텍스트 변경 리스너
        binding.editTextStorageName.doOnTextChanged { text, _, _, _ ->
            newName = text.toString().trim()
            // 저장 버튼 활성화/비활성화 로직 (이름이 비어있지 않은지 확인)
            binding.buttonSave.isEnabled = newName.isNotBlank()
        }

        // ⭐ 4. 저장 및 삭제 버튼 로직 연결 ⭐

        // 저장 버튼 클릭 리스너
        binding.buttonSave.setOnClickListener {
            // 이름과 아이콘이 변경되었을 때만 ViewModel 호출
            if (newName.isNotBlank()) {
                // ViewModel의 updateStorage 함수를 호출
                fridgeViewModel.updateStorage(
                    currentStorage.id,
                    newName,
                    newIconResName
                )
                Toast.makeText(requireContext(), "$newName 저장 공간이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                dismiss() // Bottom Sheet 닫기
            } else {
                Toast.makeText(requireContext(), "저장 공간 이름은 비워둘 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 삭제 버튼 클릭 리스너
        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }

        // 닫기 버튼
        binding.buttonClose.setOnClickListener {
            dismiss()
        }
    }

    // ⭐ 5. 삭제 확인 Dialog 표시 함수 ⭐
    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("저장 공간 삭제")
            .setMessage("저장 공간 '${currentStorage.name}'을(를) 삭제하시겠습니까?\n이 저장 공간의 모든 재료는 '정리되지 않은 재료'로 이동됩니다.")
            .setPositiveButton("삭제") { _, _ ->
                fridgeViewModel.deleteStorage(currentStorage.id)
                Toast.makeText(requireContext(), "${currentStorage.name} 저장 공간이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
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