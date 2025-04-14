package com.example.books_talk

import Book
import MyAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class UserActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var myAdapter: MyAdapter
    private lateinit var books: MutableList<Book>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        val Signout: Button = findViewById(R.id.signout_btn)
        val arrowBack: ImageView = findViewById(R.id.arrow_back_user)
        val addPostBtn: Button = findViewById(R.id.add_btn)
        val userName: TextView = findViewById(R.id.username_textView)
        val userEmail: TextView = findViewById(R.id.email_user)
        val db = Firebase.firestore

        val recyclerView: RecyclerView = findViewById(R.id.RecyclerView_User)
        books = mutableListOf()
        myAdapter = MyAdapter(books)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = myAdapter

        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username") ?: "No username"
                        val name = document.getString("name") ?: "No name"

                        userName.text = username
                        userEmail.text = name
                    } else {
                        userName.text = "User not found"
                        userEmail.text = ""
                    }
                }
                .addOnFailureListener { e ->
                    userName.text = "Error"
                    userEmail.text = ""
                    Log.e("UserActivity", "Error fetching user info", e)
                }
        }

        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { userDoc ->
                    val username = userDoc.getString("username") ?: "Unknown"

                    db.collection("books")
                        .whereEqualTo("uid", uid)
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                val title = document.getString("title") ?: ""
                                val content = document.getString("content") ?: ""
                                val rating = document.getLong("rating")?.toInt() ?: 0

                                books.add(Book(document.id, title, content, username, rating, uid))
                            }
                            myAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { exception ->
                            Log.d("UserActivity", "Error getting user posts: ", exception)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("UserActivity", "Error fetching username for posts", e)
                }
        }
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