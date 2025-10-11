// src/main/java/com/example/myapplication/feature/fridge/StorageIcon.kt

package com.example.myapplication.feature.fridge
import com.example.myapplication.R // R 파일은 그대로 사용
import android.content.Context // ⭐ 이 줄을 추가합니다. ⭐

/**
 * 저장 공간 아이콘 선택을 위한 데이터 모델
 */
data class StorageIcon(
    val iconResId: Int, // R.drawable.ic_fridge 등의 리소스 ID
    val name: String,   // 아이콘 이름 (선택적)
    val resName: String // 아이콘 리소스 이름 (예: "ic_fridge")
)

/**
 * 저장 공간으로 사용할 수 있는 아이콘 목록 (Material Icons 기반)
 */
object StorageIconData {
    // 실제 R.drawable 리소스 ID가 필요합니다.
    // 프로젝트에 ic_fridge, ic_freezer, ic_pantry 등의 Vector Asset을 추가해야 합니다.
    // 여기서는 R.drawable을 직접 사용할 수 없으므로, 임시로 리소스 이름만 사용하고
    // 나중에 R.drawable.id를 찾도록 처리합니다.

    val DEFAULT_ICON_RES_NAME = "ic_fridge"

    val iconList: List<StorageIcon> = listOf(
        // R.drawable.ic_fridge가 Vector Asset으로 존재한다고 가정합니다.
        StorageIcon(R.drawable.ic_fridge, "냉장고", "ic_fridge"),
        StorageIcon(R.drawable.ic_freezer, "냉동실", "ic_freezer"),
        StorageIcon(R.drawable.ic_pantry, "선반/창고", "ic_pantry"),
        StorageIcon(R.drawable.ic_cabinet, "수납장", "ic_cabinet"),
        StorageIcon(R.drawable.ic_box, "박스", "ic_box"),
        // ic_unorganized_box는 '정리되지 않은 재료' 박스용 아이콘이므로 제외합니다.
    )

    // 리소스 이름을 받아 R.drawable.id를 찾는 유틸리티 함수 (Context 필요)
    fun getDrawableIdByName(context: Context, resName: String): Int {
        return context.resources.getIdentifier(resName, "drawable", context.packageName)
    }
}