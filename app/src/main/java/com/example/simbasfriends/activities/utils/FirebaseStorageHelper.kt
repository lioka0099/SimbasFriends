package com.example.simbasfriends.activities.utils

import android.net.Uri
import android.util.Log
import com.example.simbasfriends.activities.models.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseStorageHelper {
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    //Upload Profile Picture & Update Firestore
    fun uploadProfilePicture(
        userId: String,
        imageUri: Uri?,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (imageUri == null) {
            Log.e("Storage", "No image selected")
            onSuccess("")
            return
        }

        val storageRef = firebaseStorage.reference.child("profile_pictures/$userId.jpg")

        storageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                storageRef.downloadUrl
            }
            .addOnSuccessListener { uri ->
                val userDocRef = firestore.collection("users").document(userId)

                userDocRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            //If user document exists, update it
                            userDocRef.update("profilePic", uri.toString())
                                .addOnSuccessListener { onSuccess(uri.toString()) }
                                .addOnFailureListener(onFailure)
                        } else {
                            //If user document does not exist, create it with profilePic
                            val newUser = mapOf("profilePic" to uri.toString())
                            userDocRef.set(newUser)
                                .addOnSuccessListener { onSuccess(uri.toString()) }
                                .addOnFailureListener(onFailure)
                        }
                    }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }


    //Upload Multiple Gallery Images & Save URLs in Firestore
    fun uploadGalleryImages(
        userId: String,
        imageUris: List<Uri>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (imageUris.isEmpty()) {
            onFailure(Exception("No images selected"))
            return
        }

        val storageRef = firebaseStorage.reference.child("users/$userId/gallery/")
        val uploadedUrls = mutableListOf<String>()

        imageUris.forEachIndexed { index, uri ->
            val fileRef = storageRef.child("${System.currentTimeMillis()}_$index.jpg")

            fileRef.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    fileRef.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    uploadedUrls.add(downloadUri.toString())

                    if (uploadedUrls.size == imageUris.size) {
                        saveGalleryImages(userId, uploadedUrls, onSuccess, onFailure)
                    }
                }
                .addOnFailureListener(onFailure)
        }
    }

    // Save Gallery Image URLs in Firestore
    private fun saveGalleryImages(
        userId: String,
        newUrls: List<String>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                val currentGallery = document.get("gallery") as? List<String> ?: emptyList()
                val updatedGallery = currentGallery + newUrls

                userDocRef.update("gallery", updatedGallery)
                    .addOnSuccessListener { onSuccess(updatedGallery) }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    //Fetch Gallery Images from Firestore
    fun getGalleryImages(userId: String, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val galleryImages = document.get("gallery") as? List<String> ?: emptyList()
                onSuccess(galleryImages)
            }
            .addOnFailureListener(onFailure)
    }

    fun deleteGalleryImage(userId: String, imageUrl: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

        // Delete the image from Firebase Storage
        storageRef.delete()
            .addOnSuccessListener {
                // Remove the image URL from the user's gallery array in Firestore
                val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)

                userDocRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val galleryList = document.get("gallery") as? MutableList<String> ?: mutableListOf()

                            if (galleryList.contains(imageUrl)) {
                                galleryList.remove(imageUrl) // Remove the image URL

                                userDocRef.update("gallery", galleryList)
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { e -> onFailure(e) }
                            } else {
                                onFailure(Exception("Image URL not found in Firestore"))
                            }
                        } else {
                            onFailure(Exception("User document does not exist"))
                        }
                    }
                    .addOnFailureListener { e -> onFailure(e) }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }



    fun uploadEventImage(
        eventId: String,
        imageUri: Uri?,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (imageUri == null) {
            Log.e("FirebaseStorageHelper", "No image selected")
            onSuccess("")  // Proceed with an empty image
            return
        }

        val storageRef = firebaseStorage.reference.child("events/$eventId.jpg")

        Log.d("FirebaseStorageHelper", "Uploading image to: ${storageRef.path}")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                Log.d("FirebaseStorageHelper", "Image uploaded, retrieving download URL")
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Unknown error during image upload")
                }
                storageRef.downloadUrl
            }
            .addOnSuccessListener { uri ->
                Log.d("FirebaseStorageHelper", "Download URL: $uri")
                onSuccess(uri.toString())
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseStorageHelper", "Failed to upload image", e)
                onFailure(e)
            }
    }

fun saveEventToFirestore(
    event: Event,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val userRef = firestore.collection("users").document(event.createdBy)
    val eventRef = firestore.collection("events").document(event.eventId)
    val userEventRef = userRef.collection("my_events").document(event.eventId)

    // Save event globally
    eventRef.set(event)
        .addOnSuccessListener {
            // After saving event, store reference in user's "my_events"
            userEventRef.set(mapOf("eventId" to event.eventId))
                .addOnSuccessListener { onSuccess() }
        }
        .addOnFailureListener(onFailure)
}
}
