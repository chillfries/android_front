// mmain/main/java/com/example/myapplication/presentation/ui/my/MyFragment.kt

package com.example.myapplication.presentation.ui.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentMyBinding

class MyFragment : Fragment() {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ⭐ 1. 사용자 정보 설정 (더미 데이터)
        binding.tvUsername.text = "김레시피"
        binding.tvEmail.text = "recipe.lover@app.com"

        // ⭐ 2. 클릭 리스너 설정 (로그아웃, 회원 탈퇴)
        binding.btnLogout.setOnClickListener {
            // 실제 구현 시, ViewModel을 통해 로그아웃 API 호출 및 AuthActivity로 이동
            Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            // 예시: findNavController().navigate(R.id.action_global_to_authActivity)
        }

        binding.btnWithdraw.setOnClickListener {
            // 실제 구현 시, ViewModel을 통해 회원 탈퇴 API 호출 및 재확인 다이얼로그 표시
            Toast.makeText(context, "회원 탈퇴 기능이 호출되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 다른 메뉴 항목 클릭 리스너 (선택 사항)
        binding.tvMenuBookmark.setOnClickListener {
            Toast.makeText(context, "북마크 목록으로 이동", Toast.LENGTH_SHORT).show()
        }
        binding.tvMenuAppSettings.setOnClickListener {
            Toast.makeText(context, "앱 설정으로 이동", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}