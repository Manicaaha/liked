package com.example.books_talk

import Book
import MyAdapter
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class UserActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var myAdapter: MyAdapter
    private lateinit var books: MutableList<Book>
    private val PICK_IMAGE_REQUEST = 71
    private var imageUri: Uri? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var avatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        storage = FirebaseStorage.getInstance()


        val Signout: Button = findViewById(R.id.signout_btn)
        val arrowBack: ImageView = findViewById(R.id.arrow_back_user)
        val addPostBtn: Button = findViewById(R.id.add_btn)
        val userName: TextView = findViewById(R.id.username_textView)
        val userEmail: TextView = findViewById(R.id.email_user)
        val night: ImageView = findViewById(R.id.darkmode_btn)
        val light: ImageView = findViewById(R.id.lightmode_btn)
        val db = Firebase.firestore
        avatar = findViewById(R.id.imageView6)

        night.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        light.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


        val recyclerView: RecyclerView = findViewById(R.id.RecyclerView_User)
        books = mutableListOf()
        myAdapter = MyAdapter(books, true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = myAdapter

        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username") ?: "No username"
                        val name = document.getString("name") ?: "No name"
                        val avatarUrl = document.getString("avatarUrl")

                        userName.text = username
                        userEmail.text = name

                        if (!avatarUrl.isNullOrEmpty()) {
                            Picasso.get().load(avatarUrl).into(avatar)
                        } else {
                            avatar.setImageResource(R.mipmap.avatar_foreground)
                        }

                    } else {
                        userName.text = "User not found"
                        userEmail.text = ""
                    }
                }
                .addOnFailureListener { e ->
                    userName.text = "Error"
                    userEmail.text = ""
                    avatar.setImageResource(R.mipmap.avatar_foreground)
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
        avatar.setOnClickListener {
            openImagePicker()
        }
    }
    private fun openImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST)
    }override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageUri?.let { uri ->
                uploadImageToFirebase(uri)
            }
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid
        val storageRef = storage.reference.child("profile_pictures/$uid.jpg")

        val uploadTask = storageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                saveProfilePictureUrl(downloadUri.toString())
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                Log.e("UserActivity", "Download URL error", e)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            Log.e("UserActivity", "Upload error", e)
        }
    }

    private fun saveProfilePictureUrl(downloadUrl: String) {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid
        val db = Firebase.firestore

        db.collection("users").document(uid)
            .update("avatarUrl", downloadUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
                Picasso.get().load(downloadUrl).into(avatar)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save profile picture", Toast.LENGTH_SHORT).show()
                Log.e("UserActivity", "Firestore update error", e)
            }
    }
}