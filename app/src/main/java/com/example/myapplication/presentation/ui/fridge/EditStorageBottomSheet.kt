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
// ⭐ 수정: domain.model 패키지의 클래스를 import 합니다. ⭐
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

    // ... (나머지 코드는 이전 단계와 거의 동일)
}