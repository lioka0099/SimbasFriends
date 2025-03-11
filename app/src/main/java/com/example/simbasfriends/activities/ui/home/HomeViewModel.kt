package com.example.simbasfriends.activities.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simbasfriends.activities.models.Event
import com.example.simbasfriends.activities.models.Participant
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class HomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _currentUserId = MutableLiveData<String>()
    val currentUserId: LiveData<String> = _currentUserId

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val searchRadiusKm = 20.0 // Radius for search in kilometers 20Km
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    init {
        _currentUserId.value = auth.currentUser?.uid ?: ""
    }

    fun fetchEvents(userLocation: LatLng) {
        firestore.collection("events")
            .get()
            .addOnSuccessListener { documents ->
                val allEvents = documents.mapNotNull { it.toObject(Event::class.java) }
                val currentTime = System.currentTimeMillis()

                val filteredEvents = allEvents.filter { event ->
                    val eventTime = dateFormat.parse("${event.eventDate} ${event.eventTime}")?.time ?: 0L
                    val eventLocation = LatLng(event.eventLocation.latitude, event.eventLocation.longitude)

                    val isFutureEvent = eventTime >= currentTime
                    val isInRange = calculateDistance(userLocation, eventLocation) <= searchRadiusKm

                    isFutureEvent && isInRange
                }

                _events.postValue(filteredEvents)
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Failed to fetch events", e)
            }
    }

    // Haversine formula to calculate distance between two points on a sphere
    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val R = 6371 // Earth's radius in km
        val latDiff = Math.toRadians(end.latitude - start.latitude)
        val lngDiff = Math.toRadians(end.longitude - start.longitude)
        val a = sin(latDiff / 2) * sin(latDiff / 2) +
                cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) *
                sin(lngDiff / 2) * sin(lngDiff / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c // Distance in km
    }

    fun fetchParticipants(event: Event,onResult: (List<Participant>) -> Unit){
        val participantsIds = event.participants

        if(participantsIds.isEmpty()) {
            onResult(emptyList())
            return
        }

        firestore.collection("users")
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

    fun joinEvent(event: Event) {
        val userId = auth.currentUser?.uid ?: return

        val eventRef = firestore.collection("events").document(event.eventId)
        val userEventRef = firestore.collection("users").document(userId).collection("my_events").document(event.eventId)

        //Update event participants
        eventRef.update("participants", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                //Add event to user's events
                userEventRef.set(mapOf("eventId" to event.eventId))
                    .addOnSuccessListener {
                        _events.value = _events.value?.map { e ->
                            if (e.eventId == event.eventId) {
                                e.copy(participants = e.participants + userId)
                            } else {
                                e
                            }
                        }
                    }
            }
    }

}