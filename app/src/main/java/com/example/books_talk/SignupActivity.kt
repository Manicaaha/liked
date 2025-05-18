package com.example.books_talk

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingDialog: Dialog
    private lateinit var avatar: ImageView
    private var imageURL: String = ""
    private lateinit var imageUri2: Uri
    private var imagePicked = false

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val data: Intent? = it.data
            val imageUri = data?.data
            imageUri2 = imageUri!!
            avatar.setImageURI(imageUri)
            imagePicked = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = Firebase.auth

        val arrowBack: ImageView = findViewById(R.id.arrow_back_singup)
        val tos: TextView = findViewById(R.id.tos)
        val myCheckBox = findViewById<CheckBox>(R.id.checkBox)
        val signupButton: Button = findViewById(R.id.signupBtn)
        val email: EditText = findViewById(R.id.emailSign)
        val password: EditText = findViewById(R.id.passwordSign)
        val username: EditText = findViewById(R.id.usernameSign)
        avatar = findViewById(R.id.avatar)
        loadingDialog = createLoadingDialog(this)

        Picasso.get().load("https://i.imgur.com/DvpvklR.png").into(avatar)

        avatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }

        arrowBack.setOnClickListener {
            onBackPressed()
        }

        // Text formatting for TOS
        val text = "Agree to terms of service *"
        val spannableString = SpannableString(text)
        spannableString.setSpan(UnderlineSpan(), 9, 25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.RED), 26, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tos.text = spannableString

        tos.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_tos, null)
            builder.setView(dialogView)
                .setTitle("Terms of Service")
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }

        myCheckBox.setOnCheckedChangeListener { _, isChecked ->
            myCheckBox.buttonTintList = ColorStateList.valueOf(
                if (isChecked) Color.parseColor("#122710") else Color.parseColor("#FFFFFF")
            )
        }

        signupButton.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()
            val usernameText = username.text.toString()

            if (emailText.isEmpty() || passwordText.isEmpty() || usernameText.isEmpty()) {
                Toast.makeText(baseContext, "Inputs cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!myCheckBox.isChecked) {
                Toast.makeText(baseContext, "You must agree to the Terms of Service to continue.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loadingDialog.show()

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        createUserInDB(this, user)
                    } else {
                        loadingDialog.dismiss()
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    }
                }
        }
    }

    private fun createUserInDB(context: Context, user: FirebaseUser?) {
        if (user == null) {
            Log.e("Auth", "User is null after sign-up")
            loadingDialog.dismiss()
            return
        }

        if (!imagePicked) {
            imageURL = "https://i.imgur.com/DvpvklR.png"
            saveUserToFirestore(context, user)
        } else {
            val storageReference = FirebaseStorage.getInstance().reference
            val imageRef = storageReference.child("images/${UUID.randomUUID()}.jpg")

            imageRef.putFile(imageUri2)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        imageURL = uri.toString()
                        saveUserToFirestore(context, user)
                    }.addOnFailureListener {
                        loadingDialog.dismiss()
                        Toast.makeText(context, "Image URL retrieval failed", Toast.LENGTH_SHORT).show()
                        Log.e("Storage", "URL error: ${it.message}")
                    }
                }.addOnFailureListener {
                    loadingDialog.dismiss()
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                    Log.e("Storage", "Upload error: ${it.message}")
                }
        }
    }

    private fun saveUserToFirestore(context: Context, user: FirebaseUser) {
        val db = Firebase.firestore
        val username: EditText = findViewById(R.id.usernameSign)

        val newUser = hashMapOf(
            "name" to user.email,
            "username" to username.text.toString(),
            "avatarUrl" to imageURL
        )

        db.collection("users").document(user.uid)
            .set(newUser)
            .addOnSuccessListener {
                loadingDialog.dismiss()
                Log.d("Firestore", "User saved successfully!")
                val intent = Intent(context, HomeActivity::class.java)
                context.startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Log.e("Firestore", "Failed to save user", e)
                Toast.makeText(context, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun createLoadingDialog(context: Context): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onResume() {
        super.onResume()
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("Main", "Zalogowany")
            Toast.makeText(baseContext, "Zalogowany", Toast.LENGTH_SHORT).show()
            startActivity(Intent(baseContext, HomeActivity::class.java))
            finish()
        } else {
            Log.d("Main", "Nie zalogowany")
        }
    }
}
