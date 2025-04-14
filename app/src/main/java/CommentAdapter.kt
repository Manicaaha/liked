package com.example.books_talk

import Comment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.books_talk.R

class CommentAdapter(private val comments: List<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val usernameTextView: TextView = holder.itemView.findViewById(R.id.cmnt_user)
        val avatarImageView: ImageView = holder.itemView.findViewById(R.id.cmnt_avatar)
        val textTextView: TextView = holder.itemView.findViewById(R.id.cmnt_text)

        textTextView.text = comments[position].content

        db.collection("users").document(comments[position].uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    usernameTextView.text = document.getString("username") ?: "Unknown User"

                } else {
                    usernameTextView.text = "Error loading user"
                }
            }
            .addOnFailureListener {
                usernameTextView.text = "Error loading user"
            }
    }

    override fun getItemCount(): Int {
        return comments.size
    }
}
