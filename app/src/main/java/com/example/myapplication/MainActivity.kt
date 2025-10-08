package com.example.myapplication // <-- build.gradle.kts의 namespace와 일치해야 합니다!

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
// 주의: 최신 Android Studio 템플릿은 ActivityMainBinding을 사용하지만,
// 여기서는 가장 기본적인 setContentView를 사용합니다.
// View Binding 활성화 후 Gradle Sync가 완료되면 자동으로 생성되는 클래스를 Import 합니다.
import com.example.myapplication.databinding.ActivityMainBinding
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {
    // 1. 바인딩 객체를 늦게 초기화할 변수로 선언합니다.
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. NavHostFragment 인스턴스를 supportFragmentManager에서 찾습니다.
        // val navController = findNavController(R.id.nav_host_fragment_activity_main) // ❌ 이 코드는 NavController를 찾지 못해 오류 발생 가능

        // FragmentContainerView의 ID를 사용합니다.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        // 2. NavHostFragment에서 NavController를 가져옵니다.
        val navController = navHostFragment.navController

        // 3. BottomNavigationView와 NavController 연결
        binding.navView.setupWithNavController(navController)
    }
}