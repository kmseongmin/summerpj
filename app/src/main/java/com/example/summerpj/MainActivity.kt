package com.example.summerpj

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.summerpj.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var Emailedt:EditText
    lateinit var Passwordedt:EditText
    lateinit var loginbtn:Button
    lateinit var Registerbtn:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Emailedt = binding.Emailedt
        Passwordedt = binding.Passwordedt
        loginbtn = binding.loginbtn
        Registerbtn = binding.Registerbtn

        //회원가입 버튼 클릭 리스너
        Registerbtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}