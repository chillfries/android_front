// src/main/java/com/example/myapplication/feature/fridge/data/Ingredient.kt (수정)

package com.example.myapplication.feature.fridge.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date // Date 타입을 사용한다면 필요

@Parcelize
data class Ingredient(
    val id: Long = 0,
    val name: String,
    val quantity: Int,
    val unit: String,
    // ⭐ 오류 해결: storageLocation 필드 추가 ⭐
    val storageLocation: String,
    val expiryDate: Date,
    // 필요하다면 다른 필드 추가...
) : Parcelable