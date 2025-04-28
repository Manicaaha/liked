import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.books_talk.R
import com.example.books_talk.SinglebookActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MyAdapter(var books: List<Book>, private val isUserActivity: Boolean) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    private var auth = Firebase.auth
    val db = Firebase.firestore
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val trashImageView: ImageView = itemView.findViewById(R.id.trash_img)//finish this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val textViewTitle: TextView = holder.itemView.findViewById(R.id.Title_TextView)
        val textViewContent: TextView = holder.itemView.findViewById(R.id.content_TextView)
        val textViewUsername: TextView = holder.itemView.findViewById(R.id.username_TextView)
        val textViewRating: TextView = holder.itemView.findViewById(R.id.rating_TextView)
        val like_fill: ImageView = holder.itemView.findViewById(R.id.imageView4)
        var doesExist = false
        val currentBook = books[position]

        textViewTitle.text = currentBook.title
        textViewContent.text = currentBook.content
        textViewUsername.text = currentBook.username
        textViewRating.text = "${currentBook.rating}"

        val docRef = db.collection("likes").document(auth.currentUser!!.uid+":"+books[position].bookId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    like_fill.setImageResource(R.mipmap.fav_filled_foreground)
                    doesExist = true
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        like_fill.setOnClickListener {

            if(!doesExist) {
                val follow = hashMapOf(
                    "uid" to auth.currentUser!!.uid,
                    "pid" to books[position].bookId
                )

                db.collection("likes").document(auth.currentUser!!.uid+":"+books[position].bookId)
                    .set(follow)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TAG", "DocumentSnapshot written with ID:")
                        like_fill.setImageResource(R.mipmap.fav_filled_foreground)
                        doesExist = true
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error adding document", e)
                    }
            }
            else {
                db.collection("likes").document(auth.currentUser!!.uid+":"+books[position].bookId)
                    .delete()
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!")
                        like_fill.setImageResource(R.mipmap.fav_foreground)
                        doesExist = false
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            }

        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, SinglebookActivity::class.java).apply {
                putExtra("bookId", books[position].bookId)
                putExtra("title", books[position].title)
                putExtra("content", books[position].content)
                putExtra("username", books[position].username)
                putExtra("rating", books[position].rating)
            }
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return books.size
    }
}

