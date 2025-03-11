package com.example.simbasfriends.activities.ui.profile

import android.app.Activity.RESULT_OK
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.simbasfriends.activities.adapters.GalleryAdapter
import com.example.simbasfriends.R
import com.example.simbasfriends.activities.dialogs.LoadingDialog
import com.example.simbasfriends.databinding.FragmentProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private var selectedProfileImageUri: Uri? = null
    private var selectedGalleryImages: MutableList<Uri> = mutableListOf()

    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        initListeners()

        // Fetch user profile when the fragment is loaded
        userId?.let { viewModel.fetchUserProfile(it) }
    }

    //Observe ViewModel for Profile Data
    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            if (userProfile != null) {
                binding.UserProfileNameTXT.text = userProfile.name
                binding.UserProfileBioTXT.text = userProfile.bio
                binding.UserProfileDogTraitsTXT.text = "Who is ${userProfile.dogProfile?.name}?"
                binding.UserProfileFriendlyTraitTXT.text = userProfile.dogProfile.friendliness
                binding.UserProfileSizeTraitTXT.text = userProfile.dogProfile.size
                binding.UserProfileEnergyTraitTXT.text = userProfile.dogProfile.energy
                binding.UserProfileDogPhotosTXT.text = "${userProfile.dogProfile.name}'s Photos"
            }

        }

        viewModel.profileImageUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.create_new_event_dog)
                .error(R.drawable.create_new_event_dog)
                .circleCrop()
                .into(binding.UserProfileProfileIMG)
        }

        viewModel.galleryImages.observe(viewLifecycleOwner) { urls ->
            binding.UserProfileDogPhotosGrid.adapter = GalleryAdapter(
                requireContext(),
                urls,
                isCurrentUserProfile = true,
                onAddPhotoClickedListener = { selectMultiplePictures() },
                onDeletePhotoClicked = { imageUrl -> viewModel.deletePhoto(userId!!, imageUrl) } // Handle delete
            )
        }
        viewModel.uploading.observe(viewLifecycleOwner) { isUploading ->
            if(isUploading) showLoading() else hideLoading()
        }
    }

    //Initialize Click Listeners
    private fun initListeners() {
        binding.UserProfileCameraBTN.setOnClickListener { selectProfilePicture() }
        binding.UserProfileSettingsBTN.setOnClickListener{
            findNavController().navigate(R.id.settingsFragment)
        }
    }

    //Image Picker for Profile Picture
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedProfileImageUri = result.data?.data
                selectedProfileImageUri?.let {
                    viewModel.uploadProfilePicture(userId!!, it) // Upload Profile Image
                }
            } else {
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }

    //Image Picker for Multiple Gallery Images
    private val multipleImagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                selectedGalleryImages = uris.toMutableList()
                viewModel.uploadGalleryImages(userId!!, selectedGalleryImages) // Upload Gallery Images
            } else {
                Toast.makeText(requireContext(), "No images selected", Toast.LENGTH_SHORT).show()
            }
        }

    //Launch Image Picker for Profile Picture
    private fun selectProfilePicture() {
        ImagePicker.with(this)
            .crop()
            .compress(512)
            .maxResultSize(512, 512)
            .createIntent { intent -> imagePickerLauncher.launch(intent) }
    }

    //Launch Gallery Picker for Multiple Images
    private fun selectMultiplePictures() {
        multipleImagePickerLauncher.launch("image/*")
    }

    private fun showLoading() {
        if(loadingDialog == null) loadingDialog = LoadingDialog()
        loadingDialog?.show(childFragmentManager,"LoadingDialog")
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
