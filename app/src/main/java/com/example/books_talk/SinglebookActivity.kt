package com.example.books_talk
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SinglebookActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentsList: MutableList<Comment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singlebook)

        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val bookId = intent.getStringExtra("bookId")
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val username = intent.getStringExtra("username")

        val titlePlace: TextView = findViewById(R.id.Title_TextView)
        val usernamePlace: TextView = findViewById(R.id.username_TextView)
        val contentPlace: TextView = findViewById(R.id.content_TextView)
        val addCmtButton: Button = findViewById(R.id.addComment_button)
        val cmntText: EditText = findViewById(R.id.singleBook_editText)
        val commentsRecyclerView: RecyclerView = findViewById(R.id.singlePost_recyclerView)
        val home: ImageView = findViewById(R.id.home_img)
        val search: ImageView = findViewById(R.id.search_img)
        val liked: ImageView = findViewById(R.id.fav_img)
        val user: ImageView = findViewById(R.id.user_btn_single)

        titlePlace.text = title
        usernamePlace.text = username
        contentPlace.text = content

        commentsList = mutableListOf()
        commentAdapter = CommentAdapter(commentsList)
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter

        db.collection("comments")
            .whereEqualTo("bookId", bookId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("SinglebookActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    commentsList.clear()
                    for (document in snapshots.documents) {
                        val comment = document.toObject(Comment::class.java)
                        if (comment != null) {
                            commentsList.add(comment)
                        }
                    }
                    commentAdapter.notifyDataSetChanged()
                }
            }

        addCmtButton.setOnClickListener {
            val currentUserId = auth.currentUser!!.uid
            val cmntContent = cmntText.text.toString()
            if (cmntContent.length > 300) {
                Toast.makeText(baseContext, "Content exceeds 300 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { userDocument ->
                    val currentUserUsername = userDocument.data?.get("username").toString()

                    val comment = hashMapOf(
                        "username" to currentUserUsername,
                        "uid" to currentUserId,
                        "content" to cmntText.text.toString(),
                        "bookId" to bookId
                    )

                    db.collection("comments")
                        .add(comment)
                        .addOnSuccessListener { documentReference ->
                            Log.d("Comment", "DocumentSnapshot written with ID: ${documentReference.id}")
                            cmntText.text.clear()
                        }
                        .addOnFailureListener { e ->
                            Log.w("Comment", "Error adding comment", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w("SingleBookActivity", "Error getting username for comment: ", e)
                }
        }

        val backArrow: ImageView = findViewById(R.id.arrow_back_cmnt)
        backArrow.setOnClickListener {
            onBackPressed()
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
        user.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
