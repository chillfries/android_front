// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // ⭐ Hilt 플러그인 추가 ⭐
    alias(libs.plugins.hilt) apply false
    // ⭐ KSP 플러그인 추가 ⭐
    alias(libs.plugins.ksp) apply false
}