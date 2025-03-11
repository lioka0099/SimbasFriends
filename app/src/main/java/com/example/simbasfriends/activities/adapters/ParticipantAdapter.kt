package com.example.simbasfriends.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.simbasfriends.activities.models.Participant
import com.example.simbasfriends.activities.utils.ParticipantDiffCallback
import com.example.simbasfriends.databinding.ItemParticipantBinding

class ParticipantAdapter(
    private var participants: List<Participant>,
    private var friendStatuses: Map<String, String>, // Dynamic friend statuses
    private val onProfileClick: (Participant) -> Unit,
    private val onAddFriendClick: (Participant) -> Unit,
    private val onCancelRequestClick: (Participant) -> Unit
) : RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder>() {

    inner class ParticipantViewHolder(private val binding: ItemParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(participant: Participant) {
            binding.participantName.text = participant.userName
            binding.participantDogName.text = participant.dogName

            // Load profile picture with Glide
            Glide.with(binding.root.context)
                .load(participant.profilePic)
                .circleCrop()
                .into(binding.participantImage)

            val status = friendStatuses[participant.userId] ?: "not_friends"

            // Handle Friend Status
            when (status) {
                "friends" -> {
                    binding.btnAddFriend.text = "Friends"
                    binding.btnAddFriend.isEnabled = false // Disable the button
                    binding.btnAddFriend.alpha = 0.5f
                }
                "pending" -> {
                    binding.btnAddFriend.text = "Pending"
                    binding.btnAddFriend.isEnabled = true
                }
                else -> {
                    binding.btnAddFriend.text = "Add Friend"
                    binding.btnAddFriend.isEnabled = true
                }
            }

            // Set Click Listeners
            binding.btnAddFriend.setOnClickListener {
                when (status) {
                    "pending" -> onCancelRequestClick(participant) // Cancel request
                    else -> onAddFriendClick(participant) // Send request
                }
            }

            binding.btnViewProfile.setOnClickListener { onProfileClick(participant) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding =
            ItemParticipantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParticipantViewHolder(binding)
    }

    override fun getItemCount(): Int = participants.size

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.bind(participants[position])
    }

    //Method to update friendStatuses
    fun updateFriendStatuses(newStatuses: Map<String, String>) {
        val diffCallback = ParticipantDiffCallback(participants, participants, friendStatuses, newStatuses)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        friendStatuses = newStatuses // Update the stored statuses
        diffResult.dispatchUpdatesTo(this)
    }


}
