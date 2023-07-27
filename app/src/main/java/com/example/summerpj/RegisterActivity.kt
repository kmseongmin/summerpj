package com.example.summerpj

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    lateinit var Nameedt: EditText
    lateinit var Carnumberedt: EditText
    lateinit var Birthdayedt: EditText
    lateinit var Emailedt: EditText
    lateinit var Passwordedt: EditText
    lateinit var Passwordcheckedt: EditText
    lateinit var Registerbtn: Button

    private var auth : FirebaseAuth? = null
    private var selectGender: String = ""
    private var email: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_register)

        Nameedt = findViewById(R.id.Nameedt)
        Carnumberedt = findViewById(R.id.Carnumberedt)
        Birthdayedt = findViewById(R.id.Birthdayedt)
        val genderBtn: RadioGroup = findViewById(R.id.Genderbtn)
        Emailedt = findViewById(R.id.Emailedt)
        Passwordedt = findViewById(R.id.Passwordedt)
        Passwordcheckedt = findViewById(R.id.Passwordcheckedt)
        Registerbtn = findViewById(R.id.Registerbtn)

        genderBtn.setOnCheckedChangeListener{ group, checkedID ->
            selectGender = when (checkedID){
                R.id.manbtn -> {
                    "남성"
                }
                R.id.womanbtn -> {
                    "여성"
                }
                else -> ""
            }
        }

        Registerbtn.setOnClickListener {
            val name = Nameedt.text.toString()
            val car = Carnumberedt.text.toString()
            var day = Birthdayedt.text.toString()
            val password1 = Passwordedt.text.toString()
            val password2 = Passwordcheckedt.text.toString()


            if (password1 == password2) {
                createAccount(name, car, day, selectGender, email, password1)
            } else {
                Toast.makeText(this, "비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createAccount(
        name: String,
        car: String,
        day: String,
        selectGender: String,
        email: String,
        password1: String,
    ) {
        this.email = Emailedt.text.toString()

        if (name.isNotEmpty() && car.isNotEmpty() && day.isNotEmpty() && selectGender.isNotEmpty()
            && email.isNotEmpty() && password1.isNotEmpty())  {
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, password1)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    Toast.makeText(this, "메일로 전송 성공", Toast.LENGTH_SHORT).show()
                                    Toast.makeText(this, "계정 생성 완료.", Toast.LENGTH_SHORT).show()
                                    // Firestore에 데이터 추가 함수 호출
                                    addUserDataToFirestore(name, car, day, selectGender, email)
                                    finish() // 가입창 종료 //
                                } else {
                                    Toast.makeText(this, "메일로 전송 실패", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        if (task.exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "다른 이메일 주소를 사용해주세요.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "계정 생성 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }
    private fun addUserDataToFirestore(name: String, car: String, day: String, selectGender: String, email: String) {
        val db = Firebase.firestore
        val user = hashMapOf(
            "name" to name,
            "car" to car,
            "day" to day,
            "gender" to selectGender,
            "email" to email
        )

        // Firestore에 데이터 추가
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                // 데이터 추가 성공
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                // 데이터 추가 실패
            }
    }

}