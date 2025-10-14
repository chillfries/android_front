package com.example.myapplication.presentation.ui.camera

import android.graphics.RectF

// 카메라가 탐지한 객체의 정보(위치, 텍스트)를 담는 데이터 클래스
data class DetectionResult(val boundingBox: RectF, val text: String)