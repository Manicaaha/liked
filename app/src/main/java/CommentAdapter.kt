package com.example.books_talk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

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

        val comment = comments[position]
        textTextView.text = comment.content

        db.collection("users").document(comment.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    usernameTextView.text = document.getString("username") ?: "Unknown User"
                    val avatarUrl = document.getString("avatarUrl")
                    if (!avatarUrl.isNullOrEmpty()) {
                        Picasso.get().load(avatarUrl).into(avatarImageView)
                    } else {
                        avatarImageView.setImageResource(R.mipmap.avatar_foreground)
                    }
                } else {
                    usernameTextView.text = "User not found"
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
