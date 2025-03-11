package com.example.simbasfriends.activities.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simbasfriends.R
import com.example.simbasfriends.activities.adapters.ParticipantAdapter
import com.example.simbasfriends.activities.models.Participant
import com.example.simbasfriends.activities.utils.ProfileNavigationListener
import com.example.simbasfriends.databinding.DialogParticipantsBinding

class ParticipantDialog(
    private val fragment: Fragment,
    private val participants: List<Participant>,
    private var friendStatuses: Map<String, String>, //Pass friend statuses
    private val onProfileClick: (Participant) -> Unit,
    private val onAddFriendClick: (Participant) -> Unit,
    private val onCancelRequestClick: (Participant) -> Unit
) : Dialog(fragment.requireContext()) {
    private lateinit var binding: DialogParticipantsBinding
    private lateinit var adapter: ParticipantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogParticipantsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        adapter = ParticipantAdapter(participants,
            friendStatuses,
            onProfileClick = { participant ->
                dismiss() // Close dialog
                fragment.findNavController().navigate(
                    R.id.navigation_user_profile,
                    Bundle().apply { putString("userId", participant.userId) }
                )
            },
            onAddFriendClick,
            onCancelRequestClick)
        binding.recyclerViewParticipants.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ParticipantDialog.adapter
        }

        binding.btnCloseDialog.setOnClickListener { dismiss() }
    }

    fun updateFriendStatuses(newStatuses: Map<String, String>) {
        this.friendStatuses = newStatuses
        adapter.updateFriendStatuses(newStatuses)
    }
}
