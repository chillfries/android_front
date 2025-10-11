package com.example.myapplication // ⭐ 반드시 최상단에 패키지 선언

import android.app.Application
import android.util.Log // ⭐ 누락된 Log import 추가
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

// ⭐ Class 선언 시작
class MyApplication : Application(), CameraXConfig.Provider {

    // getCameraXConfig() 메서드를 구현하여 안정적인 Camera2Config를 반환합니다.
    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.ERROR) // Log import 후 사용 가능
            .build()
    }
}