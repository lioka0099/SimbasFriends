package com.example.simbasfriends.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.simbasfriends.activities.models.Event
import com.example.simbasfriends.activities.utils.EventDiffCallback
import com.example.simbasfriends.databinding.ItemEventCardBinding

class EventCardAdapter(
    private var events: List<Event>,
    private val onEventClick: (Event) -> Unit,
    private val onParticipantsClick: (Event) -> Unit,
    private val onJoinClick: (Event) -> Unit,
    private val currentUserId: String
) : RecyclerView.Adapter<EventCardAdapter.EventViewHolder>() {

    inner class EventViewHolder(private val binding: ItemEventCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.eventName.text = event.eventName
            binding.eventDate.text = event.eventDate
            binding.eventTime.text = event.eventTime
            binding.eventLocation.text = event.eventLocation.address
            binding.eventDescription.text = event.eventDescription

            Glide.with(binding.root.context)
                .load(event.eventPhoto)
                .into(binding.eventImage)

            val isUserInEvent = event.participants.contains(currentUserId)

            binding.btnJoinEvent.isEnabled = !isUserInEvent
            binding.btnJoinEvent.text = if (isUserInEvent) "Joined" else "Join Meeting"
            binding.btnJoinEvent.alpha = if (isUserInEvent) 0.5f else 1.0f
            binding.btnJoinEvent.setOnClickListener { if (!isUserInEvent) onJoinClick(event) }

            binding.eventCard.setOnClickListener { onEventClick(event) }

            binding.participants.setOnClickListener { onParticipantsClick(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    fun updateEvents(newEvents: List<Event>) {
        val diffCallback = EventDiffCallback(events, newEvents)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        events = newEvents
        diffResult.dispatchUpdatesTo(this)
    }
}