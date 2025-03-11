package com.example.simbasfriends.activities.ui.friends

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _friendRequests = MutableLiveData<List<String>>()
    val friendRequests: LiveData<List<String>> get() = _friendRequests

    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> get() = _friends

    init {
        fetchFriendRequests()
        fetchFriends()
    }

    private fun fetchFriends() {
        currentUserId?.let { uid ->
            firestore.collection("users").document(uid).collection("friends")
                .get().addOnSuccessListener { documents ->
                    _friends.value = documents.map { it.id }
                }
        }
    }

    private fun fetchFriendRequests() {
        currentUserId?.let { uid ->
            firestore.collection("users").document(uid).collection("friend_requests")
                .whereEqualTo("status", "pending")
                .get().addOnSuccessListener { documents ->
                    _friendRequests.value = documents.map { it.id }
                }
        }
    }

    fun acceptFriendRequest(friendId: String) {
        currentUserId?.let { uid ->
            Log.d("FirestoreDebug", "Accepting friend request from: $friendId") // ✅ Log step

            firestore.runBatch { batch ->
                val timestamp = System.currentTimeMillis()

                // Add friend to both users
                batch.set(firestore.collection("users").document(uid).collection("friends").document(friendId), mapOf("timestamp" to timestamp))
                batch.set(firestore.collection("users").document(friendId).collection("friends").document(uid), mapOf("timestamp" to timestamp))

                // Delete the friend request
                batch.delete(firestore.collection("users").document(uid).collection("friend_requests").document(friendId))
            }.addOnSuccessListener {
                fetchFriendRequests() // Refresh UI
                fetchFriends() // Update friends list
            }.addOnFailureListener { e ->
                Log.e("FirestoreDebug", "Error accepting friend request", e) // Log step
            }
        }
    }

    fun denyFriendRequest(friendId: String) {
        currentUserId?.let { uid ->
            firestore.collection("users").document(uid).collection("friend_requests")
                .document(friendId)
                .delete().addOnSuccessListener {
                    fetchFriendRequests()
                }
        }
    }
}
