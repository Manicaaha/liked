package com.example.books_talk

import Book
import MyAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class LikedActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var myAdapter: MyAdapter
    private lateinit var books: MutableList<Book>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liked)

        books = mutableListOf()
        myAdapter = MyAdapter(books)

        val myRecyclerView: RecyclerView = findViewById(R.id.RecyclerView_Likes)
        myRecyclerView.layoutManager = LinearLayoutManager(this)
        myRecyclerView.adapter = myAdapter

        auth = Firebase.auth
        val db = Firebase.firestore

        db.collection("likes")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val pid = document.data["pid"].toString()
                    val userId = document.data["uid"].toString()

                    db.collection("books").document(pid).get()
                        .addOnSuccessListener { postDocument ->
                            val title = postDocument.data?.get("title").toString()
                            val content = postDocument.data?.get("content").toString()

                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { userDocument ->
                                    val username = userDocument.data?.get("username").toString()
                                    val rating = postDocument.data?.get("rating")?.toString()?.toIntOrNull() ?: 0
                                    books.add(Book(pid, title, content, username, rating, userId))

                                    myAdapter.notifyDataSetChanged()
                                }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("LikedActivity", "Error getting liked posts: ", exception)
            }

        // Setting up the navigation buttons
        val home: ImageView = findViewById(R.id.home_img)
        val search: ImageView = findViewById(R.id.search_img)
        val user: ImageView = findViewById(R.id.user_btn2)

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
