package com.example.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapters.ChatAdapter
import com.example.test.models.Chat
import com.example.test.utils.UserUtils
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatFragment : Fragment() {

    var chatroomId: String? = null

    lateinit var chatAdapter: ChatAdapter
    lateinit var chatRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var bundle = this.arguments

        if(bundle != null){
            chatroomId = bundle.getString("chatroomId")
        }

        chatRecyclerView = view.findViewById(R.id.chat_recycler_view)

        setUpRecyclerView()

        val enterMessage: EditText = view.findViewById(R.id.enter_message)
        val sendMessage: ImageView = view.findViewById(R.id.send_message)

        sendMessage.setOnClickListener {
            if(enterMessage.text.isNullOrEmpty()){
                return@setOnClickListener
            }

            val chatText = enterMessage.text.toString()
            val firestore = FirebaseFirestore.getInstance().collection("Chatrooms")
                .document(chatroomId!!).collection("Messages")

            val chat = Chat(chatText, UserUtils.user!!,System.currentTimeMillis(),chatroomId!!)

            firestore.document().set(chat).addOnCompleteListener {
                chatRecyclerView.scrollToPosition(chatRecyclerView.adapter?.itemCount!! - 1)
                enterMessage.text.clear()
            }
        }

    }

    private fun setUpRecyclerView(){
        val firestore = FirebaseFirestore.getInstance()
        val query = firestore.collection("Chatrooms").document(chatroomId!!).collection("Messages")
            .orderBy("time", Query.Direction.ASCENDING)

        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Chat>().setQuery(query,Chat::class.java).build()

        chatAdapter = ChatAdapter(recyclerViewOptions)

        chatRecyclerView.adapter = chatAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()
        chatAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        chatAdapter.stopListening()
    }


}