// src/main/java/com/example/myapplication/feature/fridge/StorageIconAdapter.kt

package com.example.myapplication.presentation.ui.fridge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemStorageIconBinding
import android.util.Log // Log 사용을 위해 필요
import com.example.myapplication.R // R 파일은 그대로 사용
import com.example.myapplication.presentation.model.StorageIcon
import com.example.myapplication.presentation.model.StorageIconData

/**
 * 저장 공간 아이콘 선택 Dialog의 Grid RecyclerView를 위한 어댑터
 */
class StorageIconAdapter(
    private val icons: List<StorageIcon>,
    private val onIconSelected: (StorageIcon) -> Unit // 아이콘 선택 콜백
) : RecyclerView.Adapter<StorageIconAdapter.IconViewHolder>() {

    class IconViewHolder(val binding: ItemStorageIconBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val binding = ItemStorageIconBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        val storageIcon = icons[position]

        // 1. 아이콘 리소스 ID 찾기
        val iconResId =
            StorageIconData.getDrawableIdByName(holder.itemView.context, storageIcon.resName)

        // 2. 아이콘 이미지 설정
        if (iconResId != 0) {
            holder.binding.imageIconItem.setImageResource(iconResId)
        } else {
            Log.e("StorageIconAdapter", "아이콘 리소스를 찾을 수 없습니다: ${storageIcon.resName}")
            // 임시 플레이스홀더 설정
            holder.binding.imageIconItem.setImageResource(R.drawable.ic_add)
        }

        // 3. 클릭 리스너 설정
        holder.binding.imageIconItem.setOnClickListener {
            onIconSelected(storageIcon)
        }
    }

    override fun getItemCount(): Int = icons.size
}