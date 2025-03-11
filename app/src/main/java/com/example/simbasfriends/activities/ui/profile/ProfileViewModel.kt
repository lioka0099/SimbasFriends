package com.example.simbasfriends.activities.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simbasfriends.activities.models.UserProfile
import com.example.simbasfriends.activities.utils.FirebaseStorageHelper
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: MutableLiveData<UserProfile?> get() = _userProfile

    private val _profileImageUrl = MutableLiveData<String>()
    val profileImageUrl: LiveData<String> get() = _profileImageUrl

    private val _galleryImages = MutableLiveData<List<String>>()
    val galleryImages: LiveData<List<String>> get() = _galleryImages

    private val _uploading = MutableLiveData<Boolean>()
    val uploading: LiveData<Boolean> get() = _uploading

    //Fetch User Profile from Firestore
    fun fetchUserProfile(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userProfile = document.toObject(UserProfile::class.java)
                userProfile?.let {
                    _userProfile.value = it
                    _profileImageUrl.value = it.profilePic

                    //Fetch the gallery separately
                    fetchGalleryImages(userId)
                }
            }
            .addOnFailureListener {
                _userProfile.value = null
            }
    }


    fun fetchGalleryImages(userId: String) {
        FirebaseStorageHelper.getGalleryImages(
            userId,
            onSuccess = { galleryUrls ->
                _galleryImages.value = galleryUrls
            },
            onFailure = {
                _galleryImages.value = emptyList()
            }
        )
    }


    //Upload Profile Picture & Update Firestore
    fun uploadProfilePicture(userId: String, imageUri: Uri?) {
        _uploading.value = true
        FirebaseStorageHelper.uploadProfilePicture(userId, imageUri,
            onSuccess = { url ->
                _profileImageUrl.value = url
                _uploading.value = false
            },
            onFailure = { _uploading.value = false }
        )
    }

    //Upload Multiple Gallery Images
    fun uploadGalleryImages(userId: String, imageUris: List<Uri>) {
        _uploading.value = true
        FirebaseStorageHelper.uploadGalleryImages(userId, imageUris,
            onSuccess = { updatedUrls ->
                _galleryImages.value = updatedUrls
                _uploading.value = false
            },
            onFailure = { _uploading.value = false }
        )
    }

    fun deletePhoto(userId: String, imageUrl: String) {
        _uploading.value = true

        FirebaseStorageHelper.deleteGalleryImage(userId, imageUrl,
            onSuccess = {
                fetchGalleryImages(userId) // ✅ Refresh gallery after deletion
                _uploading.value = false
            },
            onFailure = { exeption ->
                _uploading.value = false
                Log.e("ProfileViewModel", "Failed to delete photo: ${exeption.message}")
            }
        )
    }
}
