package com.example.simbasfriends.activities.ui.newEvent

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simbasfriends.activities.models.Event
import com.example.simbasfriends.activities.utils.FirebaseStorageHelper
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class NewEventViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _eventCreated = MutableLiveData<Boolean>()
    val eventCreated: LiveData<Boolean> get() = _eventCreated

    private val _dogName = MutableLiveData<String>()
    private val dogName: LiveData<String> get() = _dogName

    init {
        fetchDogName() // Fetch the dog name when ViewModel is created
    }

    private fun fetchDogName() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("dogProfile.name") ?: "My Dog"
                _dogName.value = name
            }
            .addOnFailureListener {
                Log.e("NewEventViewModel", "Error fetching dog name", it)
            }
    }

    fun createEvent(
        eventDate: String,
        eventTime: String,
        eventLocation: LatLng,
        eventAddress: String,
        eventDescription: String,
        imageUri: Uri?
    ) {
        val eventId = UUID.randomUUID().toString() // Generate a unique ID for the event
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val eventName = "${_dogName.value ?: "My Dog"}'s Meeting"

        // Upload image first to Firebase Storage
        FirebaseStorageHelper.uploadEventImage(eventId, imageUri,
            onSuccess = { imageUrl ->
                val event = Event(
                    eventId = eventId,
                    eventName = eventName,
                    eventDate = eventDate,
                    eventTime = eventTime,
                    eventLocation = Event.Location(
                        eventLocation.latitude,
                        eventLocation.longitude,
                        eventAddress
                    ),
                    eventDescription = eventDescription,
                    eventPhoto = imageUrl,
                    createdBy = userId,
                    participants = listOf(userId) // Set the creator as the first participant
                )

                //Save event to Firestore
                FirebaseStorageHelper.saveEventToFirestore(event,
                    onSuccess = {
                        _eventCreated.postValue(true) //Event successfully created
                        Log.d("NewEventViewModel", "Event created successfully")
                    },
                    onFailure = { e ->
                        Log.e("NewEventViewModel", "Failed to save event to Firestore", e)
                        _eventCreated.postValue(false) //Failed to create event
                    }
                )
            },
            onFailure = { e ->
                Log.e("NewEventViewModel", "Image upload failed", e)
                _eventCreated.postValue(false)
            }
        )
    }

    fun resetEventCreationStatus() {
        _eventCreated.postValue(false)
    }

}