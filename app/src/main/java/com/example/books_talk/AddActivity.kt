package com.example.books_talk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val titleEdit: EditText = findViewById(R.id.titleEditText)
        val contentEdit: EditText = findViewById(R.id.conentEditText)
        val ratingEdit: EditText = findViewById(R.id.rating_edit)
        val addBookBtn: Button = findViewById(R.id.addBook_btn)
        val userBtn: ImageView = findViewById(R.id.user_btn_add)
        val home: ImageView = findViewById(R.id.home_img)
        val search: ImageView = findViewById(R.id.search_img)
        val liked: ImageView = findViewById(R.id.fav_img)
        val db = Firebase.firestore
        auth = Firebase.auth
        val user = auth.currentUser

        userBtn.setOnClickListener {
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
        liked.setOnClickListener {
            val intent = Intent(this, LikedActivity::class.java)
            startActivity(intent)
            finish()
        }

        addBookBtn.setOnClickListener {
            val title = titleEdit.text.toString()
            val content = contentEdit.text.toString()
            val ratingString = ratingEdit.text.toString()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(baseContext, "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rating = try {
                val rate = ratingString.toInt()
                if (rate in 1..10) rate else {
                    Toast.makeText(baseContext, "Rating must be between 1 and 10", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(baseContext, "Invalid rating. Please enter a number between 1 and 10", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (content.length > 500) {
                Toast.makeText(baseContext, "Content exceeds 500 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val book = hashMapOf(
                "title" to titleEdit.text.toString(),
                "content" to contentEdit.text.toString(),
                "uid" to user!!.uid,
                "rating" to rating
            )

            db.collection("books")
                .add(book)
                .addOnSuccessListener {
                    Log.d("AddBook", "DocumentSnapshot successfully written!")
                    Toast.makeText(
                        baseContext,
                        "Added Book Review",
                        Toast.LENGTH_SHORT,
                    ).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e -> Log.w("AddBook", "Error writing document", e) }

        }
    }
}