package com.example.test.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.models.User
import com.example.test.utils.UserUtils
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class SearchAdapter(options: FirestoreRecyclerOptions<User>, val context: Context): FirestoreRecyclerAdapter<User, SearchAdapter.SearchViewHolder>(options) {

    class SearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val userImage: CircleImageView = itemView.findViewById(R.id.profile_image)
        val nameText: TextView = itemView.findViewById(R.id.user_name)
        val followButton: Button = itemView.findViewById(R.id.follow_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user,parent,false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int, model: User) {
        holder.nameText.text = model.name

        Glide.with(context!!)
            .load(model.imageUrl)
            .placeholder(R.drawable.person_icon)
            .centerCrop()
            .into(holder.userImage)

        if(UserUtils.user?.following?.contains(snapshots.getSnapshot(holder.adapterPosition).id)!!){
            holder.followButton.text = "Following"
        }
        else{
            holder.followButton.text = "Follow"
        }

        holder.followButton.setOnClickListener {
            val firestore = FirebaseFirestore.getInstance()
            val userDocument = firestore.collection("Users").document(UserUtils.user?.id!!)

            userDocument.get().addOnCompleteListener {
                if(it.isSuccessful){
                    val user = it.result?.toObject(User::class.java)

                    if(holder.followButton.text == "Following"){
                        user?.following?.remove(snapshots.getSnapshot(holder.adapterPosition).id)
                        user?.let{user1->
                            userDocument.set(user1)
                        }
                        holder.followButton.text = "Follow"
                    }else{
                        user?.following?.add(snapshots.getSnapshot(holder.adapterPosition).id)
                        user?.let{user1->
                            userDocument.set(user1)
                        }
                        holder.followButton.text = "Following"
                    }
                }else{
                    Toast.makeText(context, "Something went wrong. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}