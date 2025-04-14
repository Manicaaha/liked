package com.example.books_talk

import Book
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class LikedActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liked)
        val books = mutableListOf<Book>()
        val home: ImageView = findViewById(R.id.home_img)
        val search: ImageView = findViewById(R.id.search_img)
        val user: ImageView = findViewById(R.id.user_btn2)

        auth = Firebase.auth
        val db = Firebase.firestore

        db.collection("likes")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("Home", "${document.id} => ${document.data}")

                    val userId = document.data["uid"].toString()

                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { userDocument ->
                            val username = userDocument.data?.get("username").toString()

                            val rating = document.data["rating"]?.toString()?.toIntOrNull() ?: 0

                            books.add(
                                Book(
                                    document.id,
                                    document.data["title"].toString(),
                                    document.data["content"].toString(),
                                    username,
                                    rating,
                                    userId
                                )
                            )
                            myAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { exception ->
                            Log.d("Home", "Error getting username: ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Home", "Error getting documents: ", exception)
            }

        home.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        search.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
            finish()
        }
        user.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}