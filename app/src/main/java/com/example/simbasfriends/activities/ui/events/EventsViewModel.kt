package com.example.simbasfriends.activities.ui.events

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simbasfriends.activities.models.Event
import com.example.simbasfriends.activities.models.Participant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class EventsViewModel : ViewModel() {
    private val fireStore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val _eventList = MutableLiveData<List<Event>>()
    val eventList: LiveData<List<Event>> = _eventList

//    fun fetchEvents(showPastEvents: Boolean) {
//        val userEvents = userId?.let { fireStore.collection("users").document(it).collection("my_events") }
//        userEvents?.get()?.addOnSuccessListener { eventDocs ->
//            val eventIds = eventDocs.map { it.getString("eventId") ?: "" }
//            if (eventIds.isEmpty()) {
//                _eventList.postValue(emptyList())
//                return@addOnSuccessListener
//            }
//
//            fireStore.collection("events").whereIn("eventId", eventIds)
//                .get().addOnSuccessListener { documents ->
//                    val events = documents.mapNotNull { it.toObject(Event::class.java) }
//                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//
//                    val filteredEvents = events.filter { event ->
//                        val eventTimeStamp = dateFormat.parse(event.eventDate)?.time ?: 0L
//                        showPastEvents || eventTimeStamp >= System.currentTimeMillis()
//                    }
//
//                    _eventList.postValue(filteredEvents)
//                }
//                .addOnFailureListener {
//                    _eventList.postValue(emptyList())
//                }
//        }
//    }

    fun fetchEvents(showPastEvents: Boolean) {
        val userEvents = userId?.let { fireStore.collection("users").document(it).collection("my_events") }
        userEvents?.get()?.addOnSuccessListener { eventDocs ->
            val eventIds = eventDocs.map { it.getString("eventId") ?: "" }
            if (eventIds.isEmpty()) {
                _eventList.postValue(emptyList())
                return@addOnSuccessListener
            }

            fireStore.collection("events").whereIn("eventId", eventIds)
                .get().addOnSuccessListener { documents ->
                    val events = documents.mapNotNull { it.toObject(Event::class.java) }
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) // 🟢 Include Time!

                    val currentTimeMillis = System.currentTimeMillis()

                    val filteredEvents = events.filter { event ->
                        try {
                            // Combine event date & time into a full timestamp
                            val eventDateTime = dateFormat.parse("${event.eventDate} ${event.eventTime}")?.time ?: 0L
                            showPastEvents || eventDateTime >= currentTimeMillis // Compare Full Timestamp ✅
                        } catch (e: Exception) {
                            Log.e("EventsViewModel", "Error parsing date: ${event.eventDate} ${event.eventTime}", e)
                            false // Exclude events with parsing issues
                        }
                    }

                    _eventList.postValue(filteredEvents)
                }
                .addOnFailureListener {
                    _eventList.postValue(emptyList())
                }
        }
    }


    fun leaveEvent(event: Event) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val userEventRef = db.collection("users").document(userId)
            .collection("my_events").document(event.eventId)

        val eventRef = db.collection("events").document(event.eventId)

        db.runTransaction { transaction ->
            // Remove event from user's "my_events"
            transaction.delete(userEventRef)

            // Remove user from event's "participants" list
            transaction.update(eventRef, "participants", FieldValue.arrayRemove(userId))
        }.addOnSuccessListener {
            fetchEvents(false) // Refresh UI after leaving event
            Log.d("EventsViewModel", "Successfully left event")
        }.addOnFailureListener { e ->
            Log.e("EventsViewModel", "Failed to leave event", e)
        }
    }



    fun fetchParticipants(event: Event,onResult: (List<Participant>) -> Unit){
        val participantsIds = event.participants

        if(participantsIds.isEmpty()) {
            onResult(emptyList())
            return
        }

        fireStore.collection("users")
            .whereIn("userId",participantsIds)
            .get()
            .addOnSuccessListener { documents ->
                val participants = documents.documents.map { document ->
                    Participant(
                        userId = document.getString("userId") ?: "",
                        profilePic = document.getString("profilePic") ?: "",
                        userName = document.getString("name") ?: "",
                        dogName = document.getString("dogProfile.name") ?: "",
                    )
                }
                onResult(participants) // Pass the list of participants to the callback
            }
            .addOnFailureListener {
                onResult(emptyList()) // Pass an empty list in case of failure
            }
    }
}