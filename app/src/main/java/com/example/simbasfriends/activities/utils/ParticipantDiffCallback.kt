package com.example.simbasfriends.activities.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.simbasfriends.activities.models.Participant

class ParticipantDiffCallback(
    private val oldList: List<Participant>,
    private val newList: List<Participant>,
    private val oldStatuses: Map<String, String>,
    private val newStatuses: Map<String, String>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].userId == newList[newItemPosition].userId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldParticipant = oldList[oldItemPosition]
        val newParticipant = newList[newItemPosition]

        val oldStatus = oldStatuses[oldParticipant.userId] ?: "not_friends"
        val newStatus = newStatuses[newParticipant.userId] ?: "not_friends"

        return oldParticipant == newParticipant && oldStatus == newStatus
    }
}