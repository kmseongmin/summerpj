package com.example.summerpj

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class driving_recode : AppCompatActivity() {

    lateinit var scrollView: ScrollView
    lateinit var linearLayout: LinearLayout
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driving_recode)

        scrollView = findViewById(R.id.scv)
        linearLayout = findViewById(R.id.scLinear)

        val buttonLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonLayoutParams.setMargins(0, 4.dpToPx(), 0, 4.dpToPx())


        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val dataRef: CollectionReference = db.collection("recode")
        val auth = Firebase.auth
        // 현재 로그인된 사용자의 정보 가져오기
        val currentUser = auth.currentUser
        val userEmail = currentUser?.email

        dataRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if(document.getString("email") == userEmail){
                        val title = document.getString("title")
                        val address = document.getString("address")
                        val data = document.getString("data")

                        if (!title.isNullOrEmpty()) {
                            val button = Button(this)
                            button.layoutParams = buttonLayoutParams
                            button.text = "$data\n장소 : $title\n주소 : $address"
                            button.gravity = Gravity.CENTER_VERTICAL
                            button.setBackgroundColor(Color.WHITE)
                            linearLayout.addView(button)
                        }
                    }

                }


            }
            .addOnFailureListener { exception ->
                // 데이터 읽기 실패 시 처리
                // 예: Log.e("Firestore", "Error getting documents: ", exception)
            }
    }

    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
}