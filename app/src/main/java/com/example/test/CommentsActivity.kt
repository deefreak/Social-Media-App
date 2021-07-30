package com.example.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapters.CommentsAdapter
import com.example.test.models.Comment
import com.example.test.utils.UserUtils
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore

class CommentsActivity : AppCompatActivity() {

    private var postId: String? = null
    private var commentsAdapter: CommentsAdapter? = null
    private lateinit var commentsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        postId = intent.getStringExtra("postId")

        commentsRecyclerView = findViewById(R.id.comments_recycler_view)

        setUpRecyclerView()

        val commentEditText : EditText = findViewById(R.id.enter_comment)
        val sendButton: ImageView = findViewById(R.id.send_comment)

        sendButton.setOnClickListener {
            val commentText = commentEditText.text.toString()
            val firestore = FirebaseFirestore.getInstance()

            val comment = Comment(commentText,UserUtils.user!!,System.currentTimeMillis())

            firestore.collection("Posts").document(postId!!)
                .collection("Comments").document().set(comment)

            commentEditText.text.clear()
        }
    }

    private fun setUpRecyclerView(){
        val firestore = FirebaseFirestore.getInstance()
        val query = postId?.let{
            firestore.collection("Posts").document(it).collection("Comments")
        }

        val recyclerViewOptions = query?.let{
            FirestoreRecyclerOptions.Builder<Comment>().setQuery(it,Comment::class.java).build()
        }

        commentsAdapter = recyclerViewOptions?.let{
            CommentsAdapter(it,this)
        }
        commentsRecyclerView.adapter = commentsAdapter
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        commentsAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        commentsAdapter?.stopListening()
    }
}