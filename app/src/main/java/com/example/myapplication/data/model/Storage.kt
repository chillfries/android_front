package com.example.myapplication.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Storage(
    val id: Long,
    val name: String,
    val iconResName: String,
    val isDefault: Boolean = false
) : Parcelable