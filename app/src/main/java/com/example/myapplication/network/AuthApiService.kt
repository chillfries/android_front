package com.example.myapplication.network

// import 경로를 data.model 패키지로 수정합니다.
import com.example.myapplication.data.model.UserCreateRequest
import com.example.myapplication.data.model.UserLoginRequest
import com.example.myapplication.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
// ✅ Recipe 모델 import 추가
import com.example.myapplication.data.model.GroupedSearchResponse
import com.example.myapplication.data.model.SearchRequest


// API Endpoints 정의
interface AuthApiService {
    @POST("/api/v1/users/signup")
    suspend fun signup(@Body user: UserCreateRequest): Response<Unit>

    @POST("/api/v1/users/login")
    suspend fun login(@Body user: UserLoginRequest): Response<LoginResponse>

    // ✅ 레시피 통합 검색 API 정의 추가
    @POST("/api/v1/dishes/search/grouped")
    suspend fun searchRecipes(@Body searchRequest: SearchRequest): Response<GroupedSearchResponse>
}