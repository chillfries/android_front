plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // ⭐ 1. Parcelize 플러그인 추가 (Storage 모델에 @Parcelize 사용 가능하도록) ⭐
    alias(libs.plugins.kotlin.parcelize) // libs.versions.toml에 정의되어 있다고 가정

    // ⭐ 2. Safe Args 플러그인 추가 (Navigation Directions 클래스 자동 생성) ⭐
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    // 👇 이곳에 View Binding 활성화 코드를 추가합니다.
    buildFeatures {
        viewBinding = true
    }
    // 👆 View Binding 설정 완료

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        // ⭐ 1. Java 8 이상 API 사용을 위한 디슈가링 설정 추가 ⭐
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // 1. Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.constraintlayout)

    // 2. Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // 3. DataStore
    implementation(libs.androidx.datastore.preferences)

    // 4. Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // 5. Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // B-1 태스크: CameraX 및 TedPermission 추가 (libs.versions.toml에 정의됨)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.ted.permission.normal)

    // 테스트 관련 (기존 유지)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ⭐ 2. 디슈가링 라이브러리 추가 ⭐
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}