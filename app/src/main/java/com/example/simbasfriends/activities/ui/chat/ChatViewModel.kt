package com.example.simbasfriends.activities.ui.chat


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simbasfriends.activities.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private lateinit var senderId: String
    private lateinit var receiverId: String
    private var chatRef: CollectionReference? = null

    fun initChat(friendId: String) {
        senderId = auth.currentUser?.uid ?: ""
        receiverId = friendId
        chatRef = firestore.collection("users").document(senderId)
            .collection("messages").document(receiverId)
            .collection("messages")

        loadMessages()
    }

    private fun loadMessages() {
        chatRef?.orderBy("timestamp", Query.Direction.ASCENDING)
            ?.addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("ChatDebug", "Error loading messages: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshots == null || snapshots.isEmpty) {
                    Log.d("ChatDebug", "No messages found.")
                    _messages.postValue(emptyList()) // Ensure UI updates even when no messages
                    return@addSnapshotListener
                }

                val messagesList = snapshots.documents.mapNotNull { it.toObject(Message::class.java) }
                _messages.postValue(messagesList) // Use `postValue` to ensure instant UI update

                Log.d("ChatDebug", "Messages loaded successfully: ${messagesList.size} messages.")
            }
    }

fun sendMessage(text: String) {
    if (text.isEmpty()) return

    val timestamp = System.currentTimeMillis()
    val message = Message(UUID.randomUUID().toString(),senderId, receiverId, text, timestamp)

    val senderChatRef = firestore.collection("users").document(senderId)
        .collection("messages").document(receiverId)
        .collection("messages")

    val receiverChatRef = firestore.collection("users").document(receiverId)
        .collection("messages").document(senderId)
        .collection("messages")

    senderChatRef.add(message)
        .addOnSuccessListener {
            Log.d("ChatDebug", "Message successfully sent to sender's Firestore.")
        }
        .addOnFailureListener { e ->
            Log.e("ChatDebug", "Failed to send message to sender's Firestore: ${e.message}")
        }

    receiverChatRef.add(message)
        .addOnSuccessListener {
            Log.d("ChatDebug", "Message successfully sent to receiver's Firestore.")
        }
        .addOnFailureListener { e ->
            Log.e("ChatDebug", "Failed to send message to receiver's Firestore: ${e.message}")
        }
}
}