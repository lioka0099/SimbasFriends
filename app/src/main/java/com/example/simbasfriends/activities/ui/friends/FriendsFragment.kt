package com.example.simbasfriends.activities.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simbasfriends.R
import com.example.simbasfriends.activities.adapters.FriendRequestsAdapter
import com.example.simbasfriends.activities.adapters.FriendsAdapter
import com.example.simbasfriends.activities.ui.friends.FriendsViewModel
import com.example.simbasfriends.databinding.FragmentFriendsBinding

class FriendsFragment : Fragment() {
    private var _binding : FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FriendsViewModel by viewModels()
    private lateinit var friendRequestsAdapter: FriendRequestsAdapter
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        friendRequestsAdapter = FriendRequestsAdapter(listOf(), viewModel::acceptFriendRequest, viewModel::denyFriendRequest)
        friendsAdapter = FriendsAdapter(listOf(), { userId ->
            navigateToUserProfile(userId)
        }, { userId ->
            openChat(userId)
        })

        binding.recyclerViewFriendRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFriendRequests.adapter = friendRequestsAdapter

        binding.recyclerViewFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFriends.adapter = friendsAdapter

        viewModel.friendRequests.observe(viewLifecycleOwner) { friendRequests ->
            friendRequestsAdapter.updateRequests(friendRequests)
        }
        viewModel.friends.observe(viewLifecycleOwner) { friends ->
            friendsAdapter.updateFriends(friends)
        }

    }

    private fun navigateToUserProfile(userId: String) {
        val bundle = Bundle().apply {
            putString("userId", userId)
        }
        findNavController().navigate(R.id.navigation_user_profile, bundle)
    }

    private fun openChat(friendId: String) {
        val bundle = Bundle().apply {
            putString("friendId", friendId)
        }
        findNavController().navigate(R.id.chatFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}