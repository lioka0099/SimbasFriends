package com.example.simbasfriends.activities.models

data class Event(
    val eventId: String = "",
    val eventName: String = "",
    val eventDate: String = "",
    val eventTime: String = "",
    val eventLocation: Location = Location(),
    val eventDescription: String = "",
    val eventPhoto: String = "",
    val createdBy: String = "", //Creator's UID
    val participants: List<String> = emptyList()
){
    data class Location(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val address: String = ""
    )
}
