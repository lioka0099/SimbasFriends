package com.example.simbasfriends.activities.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.simbasfriends.activities.adapters.GalleryAdapter
import com.example.simbasfriends.databinding.FragmentUserProfileBinding

class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Retrieve userId from the Bundle
        userId = arguments?.getString("userId")

        // Fetch profile if userId exists
        userId?.let {
            viewModel.fetchUserProfile(it)
        }

        observeUserProfile()
        observeGallery()
    }

    private fun observeGallery() {
        userId?.let { uid ->
            viewModel.fetchGalleryImages(uid) // Ensure gallery loads for the selected user
        }

        viewModel.galleryImages.observe(viewLifecycleOwner) { urls ->
            if (urls.isNullOrEmpty()) {
                // Optional: Show a message for an empty gallery
                Toast.makeText(requireContext(), "No photos available", Toast.LENGTH_SHORT).show()
            }

            binding.UserProfileDogPhotosGrid.adapter = GalleryAdapter(
                context = requireContext(),
                imageUrls = urls ?: emptyList(),
                isCurrentUserProfile = false,
                onAddPhotoClickedListener = {}, // Pass an empty lambda to avoid the error
                onDeletePhotoClicked = {} // No delete option for other profiles
            )
        }
    }


    private fun observeUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            if (userProfile != null) {
                // Update UI with user profile data
                binding.UserProfileNameTXT.text = userProfile.name
                binding.UserProfileBioTXT.text = userProfile.bio
                binding.UserProfileDogTraitsTXT.text = "Who is ${userProfile.dogProfile?.name}?"
                binding.UserProfileFriendlyTraitTXT.text = userProfile.dogProfile.friendliness
                binding.UserProfileSizeTraitTXT.text = userProfile.dogProfile.size
                binding.UserProfileEnergyTraitTXT.text = userProfile.dogProfile.energy
                binding.UserProfileDogPhotosTXT.text = "${userProfile.dogProfile.name}'s Photos"

                Glide.with(this)
                    .load(userProfile.profilePic)
                    .circleCrop()
                    .into(binding.UserProfileProfileIMG)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}