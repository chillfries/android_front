package com.example.myapplication.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Ingredient(
    val id: Long = 0,
    val name: String,
    val quantity: Int,
    val unit: String,
    val storageLocation: String,
    val expiryDate: Date,
) : Parcelable