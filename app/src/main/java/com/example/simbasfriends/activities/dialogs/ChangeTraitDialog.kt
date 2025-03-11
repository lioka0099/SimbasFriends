package com.example.simbasfriends.activities.dialogs

import android.R
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.simbasfriends.databinding.DialogChangeTraitBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeTraitDialog(private val trait: String) : DialogFragment() {
    private lateinit var binding: DialogChangeTraitBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogChangeTraitBinding.inflate(layoutInflater)

        // Define the list of options based on the trait type
        val traitOptions = when (trait) {
            "friendliness" -> listOf(
                "🐶 Lone Wolf", "🐕 Casual Greeter",
                "🐩 Playgroup Leader", "🐾 Tail-Wagging Ambassador"
            )
            "size" -> listOf("🐕 Pocket Pup", "🐶 Mid-Sized Marvel", "🐾 Gentle Giant")
            "energy" -> listOf("🐢 Couch Potato", "🐕 Adventure Buddy", "🐾 Zoomies Expert", "🚀 Hyper Rocket")
            else -> emptyList()
        }

        //Set up dropdown menu
        val dropDown: AutoCompleteTextView = binding.dropdownTrait
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, traitOptions)
        dropDown.setAdapter(adapter)

        dropDown.setOnClickListener {
            dropDown.showDropDown()
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Change $trait")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ -> saveTraitChange(dropDown.text.toString()) }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun saveTraitChange(newValue: String) {
        if (newValue.isEmpty() || userId == null) return

        // Update the trait value in Firestore
        val updateMap = when (trait) {
            "friendliness" -> mapOf("dogProfile.friendliness" to newValue)
            "size" -> mapOf("dogProfile.size" to newValue)
            "energy" -> mapOf("dogProfile.energy" to newValue)
            else -> return
        }

        // Update Firestore document
        firestore.collection("users").document(userId).update(updateMap)
            .addOnSuccessListener {
                if (isAdded) {  //Check if fragment is attached before showing toast
                    Toast.makeText(requireContext(), "Trait updated successfully", Toast.LENGTH_SHORT).show()
                }
                dismiss() // Close dialog after success
            }
            .addOnFailureListener {
                if (isAdded) {  //Check again before showing failure toast
                    Toast.makeText(requireContext(), "Failed to update trait", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
