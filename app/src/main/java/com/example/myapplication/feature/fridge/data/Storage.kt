// src/main/java/com/example/myapplication/feature/fridge/data/Storage.kt (수정)

package com.example.myapplication.feature.fridge.data

import android.os.Parcelable // 1. 필수 import
import kotlinx.parcelize.Parcelize // 2. Parcelize 기능 사용을 위한 import

// ⭐ 3. @Parcelize 애너테이션을 추가합니다. ⭐
@Parcelize
data class Storage(
    val id: Long,
    val name: String,
    val iconResName: String,
    val isDefault: Boolean = false
) : Parcelable // ⭐ 4. Parcelable 인터페이스를 구현합니다. ⭐