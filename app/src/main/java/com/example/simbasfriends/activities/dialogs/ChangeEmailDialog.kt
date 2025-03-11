package com.example.simbasfriends.activities.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.simbasfriends.activities.LoginActivity
import com.example.simbasfriends.databinding.DialogEditFieldBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeEmailDialog : DialogFragment() {
    private lateinit var binding: DialogEditFieldBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val user = firebaseAuth.currentUser
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEditFieldBinding.inflate(layoutInflater)
        binding.editTextField.hint = "Enter new email"

        return AlertDialog.Builder(requireContext())
            .setTitle("Change Email")
            .setView(binding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { saveNewEmail(this) }
                }
            }
    }

    private fun saveNewEmail(dialog: AlertDialog) {
        val newEmail = binding.editTextField.text.toString().trim()
        if (newEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        user?.verifyBeforeUpdateEmail(newEmail)
            ?.addOnSuccessListener {
                // Email updated in FirebaseAuth
                updateEmailInFirestore(newEmail, dialog) // Update Firestore
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update email: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateEmailInFirestore(newEmail: String, dialog: AlertDialog) {
        val userId = user?.uid
        if (userId == null) {
            Log.e("ChangeEmailDialog", "User ID is null")
            return
        }

        firestore.collection("users").document(userId)
            .update("email", newEmail)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Email verification was sent!", Toast.LENGTH_SHORT).show()
                firebaseAuth.signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear backstack
                startActivity(intent)

                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update email in Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
