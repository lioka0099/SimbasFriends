package com.example.simbasfriends.activities.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.simbasfriends.databinding.DialogEditFieldBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditFieldDialog(private val field: String) : DialogFragment(){
    private var onFieldSavedListener: ((String) -> Unit)? = null
    private lateinit var binding: DialogEditFieldBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEditFieldBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Enter $field")
            .setView(binding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                if(field == "address of the event"){
                    val userAddress = binding.editTextField.text.toString().trim()
                    if(userAddress.isNotEmpty()){
                        onFieldSavedListener?.invoke(userAddress)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(context, "Please enter a valid address", Toast.LENGTH_SHORT).show()
                    }
                } else saveField(dialog)
            }
        }

        return dialog
    }

    fun setOnFieldEditedListener(listener: (String) -> Unit) {
        this.onFieldSavedListener = listener
    }


    private fun saveField(dialog: AlertDialog) {
        val newValue = binding.editTextField.text?.trim().toString()
        if (newValue.isEmpty() || userId == null) return

        val updateMap = when (field) {
            "name" -> mapOf("name" to newValue)
            "dog_name" -> mapOf("dogProfile.name" to newValue)
            "bio" -> mapOf("bio" to newValue)
            else -> return
        }

        firestore.collection("users").document(userId)
            .update(updateMap)
            .addOnSuccessListener {
                activity?.applicationContext?.let { context ->
                    Toast.makeText(context, "Field updated!", Toast.LENGTH_SHORT).show()
                }
                if (isAdded) dialog.dismiss()
            }
            .addOnFailureListener { e ->
                activity?.applicationContext?.let { context ->
                    Toast.makeText(context, "Failed to update field", Toast.LENGTH_SHORT).show()
                }
            }
    }




}