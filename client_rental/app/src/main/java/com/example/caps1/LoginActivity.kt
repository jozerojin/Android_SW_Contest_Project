package com.example.caps1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            // 노트북의 실제 IP 주소로 변경
            val retrofit = Retrofit.Builder()
                    .baseUrl("http://IP:PORT/") // 실행 시 환경에 맞게 IP와 PORT를 수정하여 사용
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            val service = retrofit.create(ApiService::class.java)
            val call = service.login(username, password)

            call.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        Log.d("LoginActivity", "Login successful: ${response.body()}")
                        Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish() // LoginActivity를 종료하여 뒤로가기 버튼으로 다시 돌아오지 않도록 함
                    } else {
                        Log.d("LoginActivity", "Login failed: ${response.errorBody()}")
                        Toast.makeText(this@LoginActivity, "아이디 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("LoginActivity", "Network error: ${t.message}")
                    Toast.makeText(this@LoginActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Retrofit 인터페이스 정의
    interface ApiService {
        @POST("login")
        @FormUrlEncoded
        fun login(
                @Field("username") username: String,
                @Field("password") password: String
        ): Call<LoginResponse>
    }

    // 서버에서 받는 응답 데이터 클래스 정의
    data class LoginResponse(
            @SerializedName("message")
            val message: String
    )
}


//안드로이드 9 (Pie) 및 이후 버전에서는 기본적으로 Cleartext 트래픽을 허용하지 않습니다.
//즉, HTTP로 요청을 보낼 때 이러한 오류가 발생합니다.
//이 문제를 해결하려면 앱의 네트워크 보안 구성을 수정하여 Cleartext 트래픽을 허용해야 합니다.
//https 권장 로컬 한정 사용
//network_security_config 처리


//로컬 개발 시 내부 아이피 등록 *network_security_config도 필수*


