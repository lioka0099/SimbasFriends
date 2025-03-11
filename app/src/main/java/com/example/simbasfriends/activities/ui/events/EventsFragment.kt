package com.example.simbasfriends.activities.ui.events

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simbasfriends.R
import com.example.simbasfriends.activities.adapters.EventAdapter
import com.example.simbasfriends.activities.adapters.ParticipantAdapter
import com.example.simbasfriends.activities.dialogs.ParticipantDialog
import com.example.simbasfriends.activities.models.Event
import com.example.simbasfriends.activities.models.Participant
import com.example.simbasfriends.activities.ui.friends.FriendRequestViewModel
import com.example.simbasfriends.activities.utils.ProfileNavigationListener
import com.example.simbasfriends.databinding.FragmentEventsBinding

class EventsFragment : Fragment(), ProfileNavigationListener {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private lateinit var eventsViewModel: EventsViewModel
    private lateinit var friendsRequestViewModel: FriendRequestViewModel
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModels
        eventsViewModel = ViewModelProvider(this)[EventsViewModel::class.java]
        friendsRequestViewModel = ViewModelProvider(this)[FriendRequestViewModel::class.java]

        // Initialize Event Adapter
        eventAdapter = EventAdapter(
            listOf(),
            onViewParticipants = { event -> showParticipantsDialog(event) },
            onLeaveEvent = { event -> eventsViewModel.leaveEvent(event) }
        )

        // Setup RecyclerView
        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }

        // Handle switch to toggle past events
        binding.switchShowPastEvents.setOnCheckedChangeListener { _, isChecked ->
            eventsViewModel.fetchEvents(isChecked)
        }

        // Observe event list updates
        eventsViewModel.eventList.observe(viewLifecycleOwner) { events ->
            eventAdapter.updateEvents(events)
        }

        // Fetch only future events initially
        eventsViewModel.fetchEvents(false)
    }

    private fun showParticipantsDialog(event: Event) {
        eventsViewModel.fetchParticipants(event) { participants ->
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
            friendsRequestViewModel.fetchFriendStatuses(participants)

            friendsRequestViewModel.friendStatuses.observe(viewLifecycleOwner) { friendStatuses ->
                if (dialog.isShowing) {
                    dialog.updateFriendStatuses(friendStatuses)
                }
            }
        }
    }

    private fun cancelFriendRequest(participant: Participant) {
        friendsRequestViewModel.cancelFriendRequest(
            participant.userId,
            onSuccess = { updateFriendStatuses() },
            onFailure = { Toast.makeText(requireContext(), "Failed to cancel request", Toast.LENGTH_SHORT).show() }
        )
    }

    private fun handleFriendRequest(participant: Participant) {
        val status = friendsRequestViewModel.friendStatuses.value?.get(participant.userId) ?: "not_friends"

        if (status == "pending") {
            cancelFriendRequest(participant)
        } else {
            friendsRequestViewModel.sendFriendRequest(
                participant.userId,
                onSuccess = { updateFriendStatuses() },
                onFailure = { Toast.makeText(requireContext(), "Failed to send request", Toast.LENGTH_SHORT).show() }
            )
        }
    }

    private fun updateFriendStatuses() {
        friendsRequestViewModel.updateFriendStatuses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onNavigateToUserProfile(userId: String) {
        val bundle = Bundle().apply {
            putString("userId", userId)
        }
        findNavController().navigate(R.id.navigation_user_profile, bundle)
    }

}