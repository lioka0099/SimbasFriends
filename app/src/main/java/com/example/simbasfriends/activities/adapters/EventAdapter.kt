package com.example.simbasfriends.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.simbasfriends.activities.models.Event
import com.example.simbasfriends.activities.utils.EventDiffCallback
import com.example.simbasfriends.databinding.ItemEventBinding

class EventAdapter(
    private var eventList: List<Event>,
    private val onViewParticipants: (Event) -> Unit,
    private val onLeaveEvent: (Event) -> Unit
    ) : RecyclerView.Adapter<EventAdapter.EventViewHolder>(){
    class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event, onViewParticipants: (Event) -> Unit, onLeaveEvent: (Event) -> Unit) {
            binding.eventName.text = event.eventName
            binding.eventDate.text = event.eventDate
            binding.eventTime.text = event.eventTime
            binding.eventDescription.text = event.eventDescription
            binding.eventLocation.text = event.eventLocation.address

            Glide.with(binding.eventImage.context)
                .load(event.eventPhoto)
                .into(binding.eventImage)

            //Handle click events for the buttons
            binding.btnViewParticipants.setOnClickListener { onViewParticipants(event) }
            binding.btnLeaveEvent.setOnClickListener { onLeaveEvent(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun getItemCount(): Int = eventList.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(eventList[position],onViewParticipants,onLeaveEvent)
    }

    fun updateEvents(newEvents: List<Event>) {
        val diffCallback = EventDiffCallback(eventList, newEvents)
        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(diffCallback)
        eventList = newEvents // Update the data set
        diffResult.dispatchUpdatesTo(this) // Update the RecyclerView
    }
}