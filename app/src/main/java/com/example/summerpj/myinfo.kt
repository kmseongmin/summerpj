package com.example.summerpj

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.summerpj.databinding.ActivityMyinfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class myinfo : AppCompatActivity() {
    private var firestore: FirebaseFirestore? = null
    lateinit var binding: ActivityMyinfoBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        database = Firebase.database.reference
        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivityMyinfoBinding.inflate(layoutInflater)
        val userEmail: String = Firebase.auth.currentUser?.email.toString()

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loadUserData(userEmail) // 로그인 정보 가져오기

        //비밀번호 찾기 버튼
        binding.PWbtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("비밀번호를 변경하시겠습니까?")
                .setPositiveButton("확인") { dialog, _ ->
                    firebaseAuth.sendPasswordResetEmail(userEmail)
                        .addOnCompleteListener { resetTask ->
                            if (resetTask.isSuccessful) {
                                Toast.makeText(this, "비밀번호 재설정 이메일을 보냈습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "비밀번호 재설정 이메일을 보내지 못했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = builder.create()

                dialog.setOnShowListener {
                    val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

                    positiveButton.setTextColor(resources.getColor(R.color.black)) // 원하는 긍정 버튼 색상으로 변경
                    negativeButton.setTextColor(resources.getColor(R.color.black)) // 원하는 부정 버튼 색상으로 변경
                }
                dialog.show()
        }

        // 수정하기 버튼 클릭
        binding.modifyBtn.setOnClickListener {
            val gender: String = when (true) {
                binding.manbtn.isChecked -> "남성"
                binding.womanbtn.isChecked -> "여성"
                else -> ""
            }
            val car: String =binding.Carnumberedt.text.toString()
            val name: String =binding.Nameedt.text.toString()
            val day: String =binding.Birthdayedt.text.toString()

            updateUserData(gender, car, name, day, userEmail)
        }
    }
    private fun loadUserData(email: String) {
        val usersCollection = firestore?.collection("users")

        usersCollection?.whereEqualTo("email", email)
            ?.get()
            ?.addOnSuccessListener {
                for (document in it.documents) {
                    binding.Nameedt.setText(document.data?.get("name")?.toString())
                    binding.Carnumberedt.setText(document.data?.get("car")?.toString())
                    when (document.data?.get("gender")?.toString()) {
                        "남성" -> binding.manbtn.isChecked = true
                        "여성" -> binding.womanbtn.isChecked = true
                    }
                    binding.Birthdayedt.setText(document.data?.get("day").toString())
                    binding.Emailedt.setText(document.data?.get("email").toString())
                }
            }
            ?.addOnFailureListener { exception ->
                Toast.makeText(this, "로그인 정보 로드 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserData(gender: String, car: String, name: String, day: String, email: String) {
        val usersCollection = firestore?.collection("users")

        usersCollection?.whereEqualTo("email", email)
            ?.get()
            ?.addOnSuccessListener {
                for (document in it.documents) {
                    val userDocument = usersCollection.document(document.id)

                    val userData = mapOf(
                        "gender" to gender,
                        "car" to car,
                        "name" to name,
                        "day" to day
                    )
                    userDocument.update(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "수정 성공", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "수정 실패", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            ?.addOnFailureListener {
                Toast.makeText(this, "수정 실패", Toast.LENGTH_SHORT).show()
            }
    }
}