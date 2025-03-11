package com.example.simbasfriends.activities.models

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val bio: String = "",
    val email: String = "",
    var profilePic: String = "",
    val gallery: List<String>? = emptyList(),
    val dogProfile: DogProfile = DogProfile() // Default empty dog profile
) {


    data class DogProfile(
        val name: String = "",
        val friendliness: String = "",
        val size: String = "",
        val energy: String = ""
    )
}
