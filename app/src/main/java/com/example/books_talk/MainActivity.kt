package com.example.books_talk

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val night: ImageView = findViewById(R.id.darkModeImageView)
        val light: ImageView = findViewById(R.id.lightModeImageView)
        val loginButton: Button = findViewById(R.id.LoginButton)
        val signupButton: Button = findViewById(R.id.SignupButton)
        loadingDialog = createLoadingDialog(this)

        night.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        light.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            loadingDialog.show()
        }
        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            loadingDialog.show()
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