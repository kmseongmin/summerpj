package com.example.summerpj

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var Emailedt: EditText
    lateinit var Passwordedt: EditText
    lateinit var Autologin: CheckBox
    lateinit var loginbtn: Button
    lateinit var Registerbtn: Button

    private var email: String = ""
    private var password: String = ""

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()

        Emailedt = findViewById(R.id.Emailedt)
        Passwordedt = findViewById(R.id.Passwordedt)
        Autologin = findViewById(R.id.Autologin)
        loginbtn = findViewById(R.id.loginbtn)
        Registerbtn = findViewById(R.id.Registerbtn)

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        // 마지막으로 사용자가 자동 로그인을 설정한 상태를 가져와서 설정
        Autologin.isChecked = sharedPreferences.getBoolean("autoLogin", false)

        // Autologin 체크박스의 상태 변경 리스너 설정
        Autologin.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit()
                .putBoolean("autoLogin", isChecked)
                .apply()
        }

        loginbtn.setOnClickListener {
            email = Emailedt.text.toString()
            password = Passwordedt.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        var user = firebaseAuth.currentUser
                        if (user != null && user.isEmailVerified) {
                            sharedPreferences.edit()
                                .putString("email", email)
                                .putString("password", password)
                                .apply()
                            Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainroomActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "이메일 인증을 해주세요", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        Registerbtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
