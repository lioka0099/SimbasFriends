package com.example.simbasfriends.activities.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.simbasfriends.activities.models.Event

class EventDiffCallback(
    private val oldList: List<Event>,
    private val newList : List<Event>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    //Check if the items are the same (compare the eventId)
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].eventId == newList[newItemPosition].eventId
    }

    //Check if the content of the items is the same
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}