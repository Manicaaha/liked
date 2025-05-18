package com.example.books_talk

import Book
import MyAdapter
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val books = mutableListOf<Book>()
        val user: ImageView = findViewById(R.id.user_btn)
        val home: ImageView = findViewById(R.id.home_img)
        val search: ImageView = findViewById(R.id.search_img)
        val favs: ImageView = findViewById(R.id.fav_img)
        val myAdapter = MyAdapter(books, false)
        val myRecyclerView: RecyclerView = findViewById(R.id.RecyclerView_Home)
        myRecyclerView.adapter = myAdapter
        myRecyclerView.layoutManager = LinearLayoutManager(this)

        auth = Firebase.auth
        val db = Firebase.firestore



        db.collection("books")
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

        user.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
            finish()
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
        favs.setOnClickListener {
            val intent = Intent(this, LikedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}