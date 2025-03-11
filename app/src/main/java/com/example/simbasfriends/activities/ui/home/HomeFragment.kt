package com.example.simbasfriends.activities.ui.home

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simbasfriends.activities.adapters.EventCardAdapter
import com.example.simbasfriends.activities.dialogs.ParticipantDialog
import com.example.simbasfriends.activities.models.Event
import com.example.simbasfriends.activities.models.Participant
import com.example.simbasfriends.activities.ui.friends.FriendRequestViewModel
import com.example.simbasfriends.activities.utils.ProfileNavigationListener
import com.example.simbasfriends.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var friendRequestViewModel: FriendRequestViewModel
    private lateinit var adapter: EventCardAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        friendRequestViewModel = ViewModelProvider(this)[FriendRequestViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Initialize Google Map
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        // Initialize RecyclerView
        homeViewModel.currentUserId.observe(viewLifecycleOwner) { userId ->
            adapter = EventCardAdapter(
                listOf(),
                onEventClick = { event -> focusOnEvent(event) },
                onParticipantsClick = {event -> showParticipantsDialog(event)},
                onJoinClick = { event -> homeViewModel.joinEvent(event) },
                currentUserId = userId
            )
            binding.recyclerViewEvents.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerViewEvents.adapter = adapter
        }


        observeEvents()

        // Fetch user's location before fetching events
        getUserLocation()
    }

    private fun showParticipantsDialog(event: Event) {
        homeViewModel.fetchParticipants(event) { participants ->
            if (participants.isEmpty()) {
                Toast.makeText(requireContext(), "No participants found", Toast.LENGTH_SHORT).show()
                return@fetchParticipants
            }

            val dialog = ParticipantDialog(
                this,
                participants,
                emptyMap(), // Initially empty
                onProfileClick = { participant ->
                    (context as? ProfileNavigationListener)?.onNavigateToUserProfile(participant.userId)
                },
                onAddFriendClick = { participant -> handleFriendRequest(participant) },
                onCancelRequestClick = { participant -> cancelFriendRequest(participant) }
            )
            dialog.show()

            // Fetch friend statuses before updating dialog
            friendRequestViewModel.fetchFriendStatuses(participants)

            friendRequestViewModel.friendStatuses.observe(viewLifecycleOwner) { friendStatuses ->
                if (dialog.isShowing) {
                    dialog.updateFriendStatuses(friendStatuses)
                }
            }
        }
    }

    private fun handleFriendRequest(participant: Participant) {
        val status = friendRequestViewModel.friendStatuses.value?.get(participant.userId) ?: "not_friends"

        if (status == "pending") {
            cancelFriendRequest(participant)
        } else {
            friendRequestViewModel.sendFriendRequest(
                participant.userId,
                onSuccess = { updateFriendStatuses() },
                onFailure = { Toast.makeText(requireContext(), "Failed to send request", Toast.LENGTH_SHORT).show() }
            )
        }
    }

    private fun cancelFriendRequest(participant: Participant) {
        friendRequestViewModel.cancelFriendRequest(
            participant.userId,
            onSuccess = { updateFriendStatuses() },
            onFailure = { Toast.makeText(requireContext(), "Failed to cancel request", Toast.LENGTH_SHORT).show() }
        )
    }

    private fun updateFriendStatuses() {
        friendRequestViewModel.updateFriendStatuses()
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return // Handle permission request separately

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,12f))
                homeViewModel.fetchEvents(userLatLng)
            }
        }
    }

    private fun observeEvents() {
        homeViewModel.events.observe(viewLifecycleOwner) { events ->
            adapter.updateEvents(events)
            googleMap.clear() // Clear previous markers before adding new ones
            for(event in events) {
                val location = LatLng(event.eventLocation.latitude, event.eventLocation.longitude)
                googleMap.addMarker(MarkerOptions().position(location).title(event.eventName))
            }
            }
        }

    private fun focusOnEvent(event: Event) {
        val location = LatLng(event.eventLocation.latitude, event.eventLocation.longitude)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    override fun onResume() {
        super.onResume();
        binding.mapView.onResume()
    }
    override fun onPause() {
        super.onPause();
        binding.mapView.onPause()
    }
    override fun onDestroyView() {
        super.onDestroyView();
        _binding = null
    }
    override fun onDestroy() {
        super.onDestroy();
    }
}