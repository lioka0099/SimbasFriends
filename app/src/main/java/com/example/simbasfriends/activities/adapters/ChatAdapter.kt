package com.example.simbasfriends.activities.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simbasfriends.activities.models.Message
import com.example.simbasfriends.databinding.ItemMessageReceivedBinding
import com.example.simbasfriends.databinding.ItemMessageSentBinding
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(
    private var messageList: List<Message>
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    sealed class MessageViewHolder(binding: androidx.viewbinding.ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(message: Message)

        class SentMessageViewHolder(private val binding: ItemMessageSentBinding) :
            MessageViewHolder(binding) {
            override fun bind(message: Message) {
                binding.messageText.text = message.message
            }
        }

        class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) :
            MessageViewHolder(binding) {
            override fun bind(message: Message) {
                binding.messageText.text = message.message
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if (viewType == 1) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            MessageViewHolder.SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            MessageViewHolder.ReceivedMessageViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderId == currentUserId) 1 else 2
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messageList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMessages(newMessages: List<Message>) {
        messageList = newMessages
        notifyDataSetChanged()
    }
}

