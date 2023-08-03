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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var Emailedt: EditText
    lateinit var Passwordedt: EditText
    lateinit var AutologinCheckBox: CheckBox
    lateinit var loginbtn: Button
    lateinit var Registerbtn: Button
    lateinit var findPwdbtn: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()

        Emailedt = findViewById(R.id.Emailedt)
        Passwordedt = findViewById(R.id.Passwordedt)

        loginbtn = findViewById(R.id.loginbtn)
        Registerbtn = findViewById(R.id.Registerbtn)
        findPwdbtn = findViewById(R.id.findPWbtn)

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        AutologinCheckBox = findViewById(R.id.Autologin)

        AutologinCheckBox.isChecked = sharedPreferences.getBoolean("Autologin", false)
        AutologinCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit()
                .putBoolean("Autologin", isChecked)
                .apply()
        }


        // 로그인 버튼
        loginbtn.setOnClickListener {
            val email = Emailedt.text.toString()
            val password = Passwordedt.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        if (user != null && user.isEmailVerified) {
                            sharedPreferences.edit()
                                .putString("email", email)
                                .putString("password", password)
                                .apply()
                            Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainroomActivity::class.java)
                            intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

        if(AutologinCheckBox.isChecked){
            val email = sharedPreferences.getString("email", null)
            val password = sharedPreferences.getString("password", null)
            if(email!!.isNotEmpty() && password!!.isNotEmpty()){
                Emailedt.setText(email)
                Passwordedt.setText(password)
                loginbtn.performClick()
            }
        }

        //회원가입 버튼
        Registerbtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        //비밀번호 찾기 버튼
        findPwdbtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("비밀번호 찾기")
            builder.setIcon(R.drawable.pwicon)

            val Email = EditText(this)
            Email.hint = "이메일을 입력해주세요."
            builder.setView(Email)

            builder.setPositiveButton("확인") { dialog, _ ->
                val email = Email.text.toString().trim()
                if (!email.isEmpty()) {
                    firebaseAuth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val signInMethods = task.result?.signInMethods
                                if (signInMethods.isNullOrEmpty()) {
                                    Toast.makeText(this, "틀린 이메일입니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    firebaseAuth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener { resetTask ->
                                            if (resetTask.isSuccessful) {
                                                Toast.makeText(this, "비밀번호 재설정 이메일을 보냈습니다.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this, "비밀번호 재설정 이메일을 보내지 못했습니다.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            } else {
                                Toast.makeText(this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }
}
