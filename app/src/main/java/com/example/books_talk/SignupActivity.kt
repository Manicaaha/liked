package com.example.books_talk

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        val db = Firebase.firestore
        val button: Button = findViewById(R.id.googlebtnsingup)
        val arrowBack: ImageView = findViewById(R.id.arrow_back_singup)
        val textView: TextView = findViewById(R.id.tos)
        val myCheckBox = findViewById<CheckBox>(R.id.checkBox)
        val singupButton: Button = findViewById(R.id.signupBtn)
        val email: EditText = findViewById(R.id.emailSign)
        val password: EditText = findViewById(R.id.passwordSign)
        val username: EditText = findViewById(R.id.usernameSign)
        loadingDialog = createLoadingDialog(this)

        //resizing DO NOT TOUCH
        val drawable = ContextCompat.getDrawable(this, R.mipmap.google_foreground)
        val drawableWidth = 90
        val drawableHeight = 90
        drawable?.setBounds(0, 0, drawableWidth, drawableHeight)
        button.setCompoundDrawables(drawable, null, null, null)

        arrowBack.setOnClickListener {
            onBackPressed()
        }

        //text formating DO NOT TOUCH
        val text = "Agree to terms of service *"
        val spannableString = SpannableString(text)
        val underlineSpan = UnderlineSpan()
        spannableString.setSpan(underlineSpan, 9, 25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val redColorSpan = ForegroundColorSpan(Color.RED)
        spannableString.setSpan(redColorSpan, 26, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString

        myCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                myCheckBox.buttonTintList = ColorStateList.valueOf(Color.parseColor("#122710"))
            } else {
                myCheckBox.buttonTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
            }
        }

        singupButton.setOnClickListener {
            loadingDialog.show()
            if(email.text.isEmpty() || password.text.isEmpty() || username.text.isEmpty()){
                Toast.makeText(
                    baseContext,
                    "Inputs cannot be empty",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        if (user == null) {
                            Log.e("Auth", "User is null, cannot add to Firestore!")
                            return@addOnCompleteListener
                        }

                        val newUser = hashMapOf(
                            "name" to user.email,
                            "username" to username.text.toString()
                        )

                        db.collection("users").document(user.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                Log.d("Firestore", "User added successfully!")
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error writing document", e)
                                Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("Main", "Nie zalogowany")
        }
        else {
            Log.d("Main", "Zalogowany")
            Toast.makeText(
                baseContext,
                "Zalogowany",
                Toast.LENGTH_SHORT,
            ).show()
            val intent = Intent(baseContext, HomeActivity::class.java)
            startActivity(intent)
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
}