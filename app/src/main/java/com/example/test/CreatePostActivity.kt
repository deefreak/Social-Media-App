package com.example.test

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.test.models.Post
import com.example.test.models.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class CreatePostActivity : AppCompatActivity() {

    lateinit var postImage: ImageView
    private var imageUri: Uri? = null

    companion object {
        const val TAG = "CreatePostActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        postImage = findViewById(R.id.post_image)
        val postText: TextView = findViewById(R.id.post_text)
        val postButton: Button = findViewById(R.id.post_button)

        postImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080,1080)
                .start()

        }

        postButton.setOnClickListener {
            val text = postText.text.toString()

            if (TextUtils.isEmpty(text)) {
                Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            addPost(text)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(resultCode){
            Activity.RESULT_OK -> {
                val fileUri = data?.data
                postImage.setImageURI(fileUri)
                imageUri = fileUri
            }
            ImagePicker.RESULT_ERROR ->{
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, "Task cancelled", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addPost(text: String){

        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("Users")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!).get()
            .addOnCompleteListener {
                val user = it.result?.toObject(User::class.java)

                val storage = FirebaseStorage.getInstance().reference.child("Images")
                    .child(FirebaseAuth.getInstance().currentUser?.email.toString() + "_" + System.currentTimeMillis() + ".jpg" )

                val uploadTask = storage.putFile(imageUri!!)

                uploadTask.continueWithTask { task->
                    if(!task.isSuccessful){
                        Log.d("Upload Task", task.exception.toString())
                    }
                    storage.downloadUrl
                }.addOnCompleteListener { urlTaskCompleted ->
                    if(urlTaskCompleted.isSuccessful){
                        val downloadUri = urlTaskCompleted.result

                        val post = Post(text,downloadUri.toString(),user!!,System.currentTimeMillis())

                        firestore.collection("Posts").document().set(post)
                            .addOnCompleteListener { posted->
                                if(posted.isSuccessful){
                                    Toast.makeText(this, "Posted successfully!", Toast.LENGTH_LONG).show()
                                    finish()
                                }else{
                                    Toast.makeText(this, "Error occurred. Please try again.", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                    else{
                        Log.d(TAG, urlTaskCompleted.exception.toString())
                    }
                }
            }

    }
}