package com.example.simbasfriends.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.simbasfriends.activities.dialogs.LoadingDialog
import com.example.simbasfriends.activities.models.UserProfile
import com.example.simbasfriends.activities.utils.FirebaseStorageHelper
import com.example.simbasfriends.databinding.ActivityRegisterBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var selectedImageUri: Uri? = null
    private var loadingDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        initViews()
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    selectedImageUri = result.data?.data!!
                    Glide.with(this)
                        .load(selectedImageUri).circleCrop()
                        .into(binding.registerActivityProfileImageView)
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun initViews() {
        setupDropdown(
            binding.registerActivityFriendlinessDropdown,
            listOf(
                "🐶 Lone Wolf",
                "🐕 Casual Greeter",
                "🐩 Playgroup Leader",
                "🐾 Tail-Wagging Ambassador"
            )
        )
        setupDropdown(
            binding.registerActivitySizeDropdown,
            listOf("🐕 Pocket Pup", "🐶 Mid-Sized Marvel", "🐾 Gentle Giant")
        )
        setupDropdown(
            binding.registerActivityEnergyDropdown,
            listOf("🐢 Couch Potato", "🐕 Adventure Buddy", "🐾 Zoomies Expert", "🚀 Hyper Rocket")
        )

        binding.registerActivitySignUpButton.setOnClickListener {
            registerUser()
        }
        binding.activityRegisterBackArrow.setOnClickListener {
            finish()
        }

        binding.registerActivitySelectPicButton.setOnClickListener {
            ImagePicker.with(this)
                .crop().createIntent { intent ->
                    imagePickerLauncher.launch(intent)
                }
        }
    }

    private fun registerUser() {
        val fullName = binding.registerActivityNameTextedit.text.toString().trim()
        val email = binding.registerRegisterActivityEmailTextedit.text.toString().trim()
        val password = binding.registerActivityPasswordTextedit.text.toString().trim()
        val confirmPassword = binding.registerActivityConfirmPasswordTextedit.text.toString().trim()

        val dogName = binding.registerActivityDogNameTextedit.text.toString().trim()
        val friendliness = binding.registerActivityFriendlinessDropdown.text.toString()
        val size = binding.registerActivitySizeDropdown.text.toString()
        val energy = binding.registerActivityEnergyDropdown.text.toString()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || dogName.isEmpty() ||
            friendliness.isEmpty() || size.isEmpty() || energy.isEmpty()
        ) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidFullName(fullName)) {
            Toast.makeText(
                this,
                "Please enter you full name (first & lat name)",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidPassword(password)) {
            Toast.makeText(
                this,
                "Password must be at least 6 characters, contain one uppercase letter and one number.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        showLoading()

        createFirebaseUser(fullName, email, password, dogName, friendliness, size, energy)
    }

    private fun isValidFullName(fullName: String): Boolean {
        val nameRegex = "^[A-Za-z]+\\s[A-Za-z]+.*$".toRegex()
        return fullName.matches(nameRegex)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Z])(?=.*\\d).{6,}$".toRegex()
        return password.matches(passwordRegex)
    }

    private fun createFirebaseUser(
        fullName: String,
        email: String,
        password: String,
        dogName: String,
        friendliness: String,
        size: String,
        energy: String
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val userId = user?.uid
                    if (userId != null) {
                        // Upload profile picture before saving user profile
                        FirebaseStorageHelper.uploadProfilePicture(
                            userId, selectedImageUri,
                            onSuccess = { profilePictureUrl ->
                                saveUserProfile(
                                    fullName,
                                    email,
                                    dogName,
                                    friendliness,
                                    size,
                                    energy,
                                    userId,
                                    profilePictureUrl
                                )
                            },
                            onFailure = { e ->
                                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT)
                                    .show()
                                saveUserProfile(
                                    fullName,
                                    email,
                                    dogName,
                                    friendliness,
                                    size,
                                    energy,
                                    userId,
                                    ""
                                ) // Proceed without image
                            }
                        )
                    }
                } else {
                    hideLoading()
                    Toast.makeText(
                        this,
                        "Registration Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserProfile(
        fullName: String,
        email: String,
        dogName: String,
        friendliness: String,
        size: String,
        energy: String,
        userId: String,
        profilePictureUrl: String
    ) {
        val userProfile = UserProfile(
            userId = userId,
            name = fullName,
            email = email,
            bio = "Hello everyone! My name is ${fullName} and I'm ${dogName}'s parent",
            profilePic = profilePictureUrl,
            dogProfile = UserProfile.DogProfile(
                name = dogName,
                friendliness = friendliness,
                size = size,
                energy = energy
            )
        )

        firestore.collection("users").document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                Log.d(
                    "Firestore",
                    "User profile saved successfully with image URL: $profilePictureUrl"
                )
                hideLoading()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                hideLoading()
                Toast.makeText(this, "Failed to save profile!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading() {
        if(loadingDialog == null) loadingDialog = LoadingDialog()
        loadingDialog?.show(supportFragmentManager, "LoadingDialog")
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun setupDropdown(dropdown: AutoCompleteTextView, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        dropdown.setAdapter(adapter)

        dropdown.setOnClickListener {
            dropdown.showDropDown()
        }

        dropdown.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            dropdown.setText(selectedItem, false)
        }
    }
}