// 파일: mmain/main/java/com/example/myapplication/domain/model/Ingredient.kt

package com.example.myapplication.domain.model

import android.os.Parcelable
import androidx.room.Entity // ✅ import 추가
import androidx.room.PrimaryKey // ✅ import 추가
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "ingredients") // ✅ Entity 어노테이션 추가
data class Ingredient(
    @PrimaryKey(autoGenerate = true) // ✅ PrimaryKey 어노테이션 추가
    val id: Long = 0,
    val name: String,
    val quantity: Int,
    val unit: String,
    val storageLocation: String,
    val expiryDate: Date,
) : Parcelable