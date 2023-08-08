package com.example.summerpj

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class driving_recode : AppCompatActivity() {

    lateinit var recodeContent: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driving_recode)

        recodeContent = findViewById(R.id.recodeContent)
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val dataRef: CollectionReference = db.collection("recode")

        dataRef.get()
            .addOnSuccessListener { documents ->
                val stringBuilder = StringBuilder()
                for (document in documents) {
                    val title = document.getString("title")
                    val address = document.getString("address")
                    val data = document.getString("data")

                    if (!title.isNullOrEmpty()) {
                        stringBuilder.append("title: $title\n")
                        stringBuilder.append("address: $address\n")
                        stringBuilder.append("data: $data\n")
                    }
                }

                recodeContent.text = stringBuilder.toString()
            }
            .addOnFailureListener { exception ->
                // 데이터 읽기 실패 시 처리
                // 예: Log.e("Firestore", "Error getting documents: ", exception)
            }



    }
}