package com.example.simbasfriends.activities.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.simbasfriends.databinding.DialogEditFieldBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordDialog : DialogFragment() {
    private lateinit var binding: DialogEditFieldBinding
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEditFieldBinding.inflate(layoutInflater)
        binding.editTextField.hint = "Enter new password"

        return AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(binding.root)
            .setPositiveButton("Next", null)
            .setNegativeButton("Cancel", null)
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        showReAuthenticationDialog(this)
                    }
                }
            }
    }

    private fun showReAuthenticationDialog(passwordDialog: AlertDialog) {
        val currentPasswordInput = DialogEditFieldBinding.inflate(layoutInflater)
        currentPasswordInput.editTextField.hint = "Enter current password"

        AlertDialog.Builder(requireContext())
            .setTitle("Verify Identity")
            .setMessage("For security, please enter your current password.")
            .setView(currentPasswordInput.root)
            .setPositiveButton("Verify", null)
            .setNegativeButton("Cancel", null)
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val currentPassword = currentPasswordInput.editTextField.text.toString().trim()
                        if (currentPassword.isNotEmpty()) {
                            reAuthenticateUser(currentPassword, passwordDialog, this)
                        } else {
                            Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }.show()
    }

    private fun reAuthenticateUser(currentPassword: String, passwordDialog: AlertDialog, authDialog: AlertDialog) {
        val email = user?.email ?: return
        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                Log.d("ChangePasswordDialog", "Re-authentication successful")
                authDialog.dismiss() //lose re-authentication dialog
                updatePassword(passwordDialog) //Proceed to password update
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Re-authentication failed! Wrong password?", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePassword(passwordDialog: AlertDialog) {
        val newPassword = binding.editTextField.text.toString().trim()
        if (newPassword.length < 6) {
            Toast.makeText(requireContext(), "Password too short!", Toast.LENGTH_SHORT).show()
            return
        }

        user?.updatePassword(newPassword)
            ?.addOnSuccessListener {
                Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show()
                passwordDialog.dismiss()
            }
            ?.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show()
            }
    }
}
