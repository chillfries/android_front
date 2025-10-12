package com.example.myapplication.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "ingredients") // 데이터베이스 테이블 이름 지정
data class Ingredient(
    @PrimaryKey(autoGenerate = true) // id를 자동 증가하는 기본 키로 설정
    val id: Long = 0,
    val name: String,
    val quantity: Int,
    val unit: String,
    val storageLocation: String,
    val expiryDate: Date,
) : Parcelable