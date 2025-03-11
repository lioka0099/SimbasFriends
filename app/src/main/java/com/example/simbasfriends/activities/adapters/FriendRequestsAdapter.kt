package com.example.simbasfriends.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.simbasfriends.activities.utils.FriendsDiffCallback
import com.example.simbasfriends.databinding.ItemFriendRequestBinding
import com.google.firebase.firestore.FirebaseFirestore


class FriendRequestsAdapter(
    private var friendRequests: List<String>, //list of user ids
    private val onAccept: (String) -> Unit,
    private val onDeny: (String) -> Unit
) : RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder>(){
    class ViewHolder(val binding: ItemFriendRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userId: String, onAccept: (String) -> Unit, onDeny: (String) -> Unit) {
            val firestore = FirebaseFirestore.getInstance()

            //Fetch user name and profile picture
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        binding.userName.text = document.getString("name")
                        binding.userDogName.text = document.getString("dogProfile.name")
                        val profilePicUrl = document.getString("profilePic")
                        Glide.with(binding.userProfileImage.context)
                            .load(profilePicUrl).circleCrop().into(binding.userProfileImage)
                    }

                    //Handle accept and deny buttons
                    binding.btnAccept.setOnClickListener {
                        onAccept(userId)
                    }
                    binding.btnDeny.setOnClickListener {
                        onDeny(userId)
                    }
                }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemFriendRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friendRequests[position], onAccept, onDeny)
    }

    override fun getItemCount(): Int = friendRequests.size

    fun updateRequests(newRequests: List<String>) {
        val diffCallback = FriendsDiffCallback(friendRequests, newRequests)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        friendRequests = newRequests
        diffResult.dispatchUpdatesTo(this)
    }
}