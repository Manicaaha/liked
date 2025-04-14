package com.example.books_talk

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class UserActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        auth = Firebase.auth

        val Signout: Button = findViewById(R.id.signout_btn)
        val arrowBack: ImageView = findViewById(R.id.arrow_back_user)
        val addPostBtn: Button = findViewById(R.id.add_btn)
        val db = Firebase.firestore

        arrowBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        Signout.setOnClickListener {
            Firebase.auth.signOut()
            val intentRegister = Intent(this, MainActivity::class.java)
            startActivity(intentRegister)
            finish()
        }
        addPostBtn.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}