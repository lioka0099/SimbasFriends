package com.example.simbasfriends.activities.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simbasfriends.activities.models.Participant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendRequestViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _friendStatuses = MutableLiveData<Map<String, String>>() // Maps userId to status
    val friendStatuses: LiveData<Map<String, String>> get() = _friendStatuses

    fun fetchFriendStatuses(participants: List<Participant>) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // fetch all docs in the current user's "friends" subcollection
        firestore.collection("users")
            .document(currentUserId)
            .collection("friends")
            .get()
            .addOnSuccessListener { friendsCollection ->
                // Convert doc IDs into a set of friend IDs
                val friendIds = friendsCollection.documents.map { it.id }.toSet()

                // Build a map of participantId -> status
                val friendStatusesMap = mutableMapOf<String, String>()

                // Firestore check for each participant not in "friendIds"
                val total = participants.size
                var processedCount = 0

                fun checkAndPostValue() {
                    processedCount++
                    if (processedCount == total) {
                        // Once all participants are processed, post the result
                        _friendStatuses.postValue(friendStatusesMap)
                    }
                }

                for (participant in participants) {
                    if (friendIds.contains(participant.userId)) {
                        // Already friends
                        friendStatusesMap[participant.userId] = "friends"
                        checkAndPostValue()
                    } else {
                        // Not in the current user's friends => check if there's a "pending" doc
                        val participantRequestRef = firestore.collection("users")
                            .document(participant.userId)
                            .collection("friend_requests")
                            .document(currentUserId)

                        participantRequestRef.get()
                            .addOnSuccessListener { requestDoc ->
                                if (requestDoc.exists()) {
                                    friendStatusesMap[participant.userId] = "pending"
                                } else {
                                    friendStatusesMap[participant.userId] = "not_friends"
                                }
                                checkAndPostValue()
                            }
                            .addOnFailureListener {
                                friendStatusesMap[participant.userId] = "not_friends"
                                checkAndPostValue()
                            }
                    }
                }
            }
            .addOnFailureListener {
                // In case we fail to read the friends subcollection entirely,
                // fallback to "not_friends" for everyone
                val fallbackMap = participants.associate { it.userId to "not_friends" }
                _friendStatuses.postValue(fallbackMap)
            }
    }


    fun sendFriendRequest(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val requestRef = firestore.collection("users").document(userId)
            .collection("friend_requests").document(currentUserId)

        val requestData = mapOf(
            "status" to "pending",
            "timestamp" to FieldValue.serverTimestamp()
        )

        requestRef.set(requestData)
            .addOnSuccessListener {
                // Fetch updated friend statuses to reflect in UI
                fetchFriendStatuses(listOf(Participant(userId = userId)))

                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }



    fun cancelFriendRequest(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val requestRef = firestore.collection("users").document(userId)
            .collection("friend_requests").document(currentUserId)

        requestRef.delete()
            .addOnSuccessListener {
                // Update UI immediately after canceling request
                _friendStatuses.value = _friendStatuses.value?.toMutableMap()?.apply {
                    this[userId] = "not_friends"
                }
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateFriendStatuses() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(currentUserId)
            .collection("friend_requests")
            .get()
            .addOnSuccessListener { requestDocs ->
                val sentRequests = requestDocs.documents.map { it.id } // IDs of users with pending requests

                firestore.collection("users").document(currentUserId)
                    .collection("friends")
                    .get()
                    .addOnSuccessListener { friendsDocs ->
                        val friends = friendsDocs.documents.map { it.id } // IDs of confirmed friends

                        val updatedStatuses = mutableMapOf<String, String>()
                        sentRequests.forEach { updatedStatuses[it] = "pending" }
                        friends.forEach { updatedStatuses[it] = "friends" }

                        _friendStatuses.postValue(updatedStatuses) // Ensure UI updates
                    }
            }
    }

}