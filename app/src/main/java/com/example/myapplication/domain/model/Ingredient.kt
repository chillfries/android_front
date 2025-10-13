package com.example.myapplication.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "ingredients") // ✅ DB 테이블로 사용하도록 어노테이션 추가
data class Ingredient(
    @PrimaryKey(autoGenerate = true) // ✅ id를 기본 키로 설정
    val id: Long = 0,
    val name: String,
    val quantity: Int,
    val unit: String,
    val storageLocation: String,
    val expiryDate: Date,
) : Parcelable