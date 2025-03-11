package com.example.simbasfriends.activities.utils

import androidx.recyclerview.widget.DiffUtil

class FriendsDiffCallback(
    private val oldList: List<String>,
    private val newList: List<String>
) :DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition] // Compare user IDs
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition] // Same content if ID matches
    }
}