package com.example.simbasfriends.activities.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simbasfriends.activities.adapters.ChatAdapter
import com.example.simbasfriends.activities.models.Message
import com.example.simbasfriends.databinding.FragmentChatBinding

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()

    private lateinit var adapter: ChatAdapter
    private val messagesList = mutableListOf<Message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val friendId = arguments?.getString("friendId") ?: return
    viewModel.initChat(friendId)

    setupRecyclerView()

    viewModel.messages.observe(viewLifecycleOwner) { messages ->
        messagesList.clear()
        messagesList.addAll(messages)
        adapter.updateMessages(messagesList)

        // Auto-scroll to the latest message
        if (messagesList.isNotEmpty()) {
            binding.recyclerViewMessages.post {
                binding.recyclerViewMessages.smoothScrollToPosition(messagesList.size - 1)
            }
        }
    }

    binding.sendButton.setOnClickListener {
        val text = binding.messageInput.text.toString().trim()
        if (text.isNotEmpty()) {
            viewModel.sendMessage(text)
            binding.messageInput.setText("")

            // Ensure new messages are always visible
                if (messagesList.isNotEmpty()) {
                    binding.recyclerViewMessages.post {
                        binding.recyclerViewMessages.smoothScrollToPosition(messagesList.size - 1)
                    }
                }
        }
    }
}


    private fun setupRecyclerView() {
        adapter = ChatAdapter(messagesList)
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.recyclerViewMessages.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
