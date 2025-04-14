package com.example.books_talk

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val home: ImageView = findViewById(R.id.home_img)
        val user: ImageView = findViewById(R.id.user_btn_search)
        val liked: ImageView = findViewById(R.id.fav_img)

        home.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        user.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
            finish()
        }
        liked.setOnClickListener {
            val intent = Intent(this, LikedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}