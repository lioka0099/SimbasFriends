package com.example.simbasfriends.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.simbasfriends.activities.utils.FriendsDiffCallback
import com.example.simbasfriends.databinding.ItemFriendBinding
import com.google.firebase.firestore.FirebaseFirestore

class FriendsAdapter(
    private var friends: List<String>,
    private val onProfileClick: (String) -> Unit,
    private val onChatClick: (String) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>(){
    class ViewHolder(private val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userId: String, onProfileClick: (String) -> Unit, onChatClick: (String) -> Unit) {
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        binding.userName.text = document.getString("name")
                        binding.userDogName.text = document.getString("dogProfile.name")
                        val profilePicUrl = document.getString("profilePic")
                        Glide.with(binding.userProfileImage.context)
                            .load(profilePicUrl).circleCrop().into(binding.userProfileImage)
                    }

                    binding.btnProfile.setOnClickListener { onProfileClick(userId) }
                    binding.btnChat.setOnClickListener { onChatClick(userId) }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = friends.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friends[position], onProfileClick, onChatClick)
    }

    fun updateFriends(newFriends: List<String>){
        val diffCallback = FriendsDiffCallback(friends, newFriends)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        friends = newFriends
        diffResult.dispatchUpdatesTo(this)
    }

}