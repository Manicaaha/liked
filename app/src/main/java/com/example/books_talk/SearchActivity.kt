package com.example.books_talk

import Book
import MyAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var myAdapter: MyAdapter
    private val books = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val home: ImageView = findViewById(R.id.home_img)
        val user: ImageView = findViewById(R.id.user_btn_search)
        val liked: ImageView = findViewById(R.id.fav_img)
        val searchText: EditText = findViewById(R.id.SearchText)
        val SearchView: RecyclerView = findViewById(R.id.SearchRecyclerView)

        db = FirebaseFirestore.getInstance()

        myAdapter = MyAdapter(books, false)
        SearchView.adapter = myAdapter
        SearchView.layoutManager = LinearLayoutManager(this)

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

        searchText.addTextChangedListener {
            val text = it.toString().trim()
            if (text.isNotEmpty()) {
                textSearch(text.lowercase())
            } else {
                books.clear()
                myAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun textSearch(query: String) {
        db.collection("books")
            .get()
            .addOnSuccessListener { result ->
                books.clear()
                for (document in result) {
                    val bookTitle = document.getString("title").orEmpty().lowercase()
                    val userId = document.getString("uid").orEmpty()
                    val username = document.getString("username").orEmpty()

                    if (username.isEmpty() && bookTitle.contains(query)) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { userDocument ->
                                val fetchedUsername = userDocument.getString("username").orEmpty()
                                val book = Book(
                                    document.id,
                                    document.getString("title").orEmpty(),
                                    document.getString("content").orEmpty(),
                                    fetchedUsername,
                                    document.getLong("rating")?.toInt() ?: 0,
                                    userId
                                )
                                books.add(book)
                                myAdapter.notifyDataSetChanged()
                            } .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error fetching books: ${exception.message}", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }
}
