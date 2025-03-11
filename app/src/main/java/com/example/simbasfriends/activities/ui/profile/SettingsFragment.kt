package com.example.simbasfriends.activities.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.simbasfriends.R
import com.example.simbasfriends.activities.WelcomeActivity
import com.example.simbasfriends.activities.dialogs.ChangeEmailDialog
import com.example.simbasfriends.activities.dialogs.ChangePasswordDialog
import com.example.simbasfriends.activities.dialogs.ChangeTraitDialog
import com.example.simbasfriends.activities.dialogs.EditFieldDialog
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Hide bottom navigation bar
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        //Profile Settings
        findPreference<Preference>("edit_name")?.setOnPreferenceClickListener {
            EditFieldDialog("name").show(parentFragmentManager, "EditFieldDialog")
            true
        }
        findPreference<Preference>("edit_dog_name")?.setOnPreferenceClickListener {
            EditFieldDialog("dog_name").show(parentFragmentManager, "EditFieldDialog")
            true
        }
        findPreference<Preference>("edit_bio")?.setOnPreferenceClickListener {
            EditFieldDialog("bio").show(parentFragmentManager, "EditFieldDialog")
            true
        }

        //Dog Traits Settings
        findPreference<Preference>("edit_friendliness")?.setOnPreferenceClickListener {
            ChangeTraitDialog("friendliness").show(parentFragmentManager, "ChangeTraitDialog")
            true
        }
        findPreference<Preference>("edit_size")?.setOnPreferenceClickListener {
            ChangeTraitDialog("size").show(parentFragmentManager, "ChangeTraitDialog")
            true
        }
        findPreference<Preference>("edit_energy")?.setOnPreferenceClickListener {
            ChangeTraitDialog("energy").show(parentFragmentManager, "ChangeTraitDialog")
            true
        }

        //Security Settings
        findPreference<Preference>("change_email")?.setOnPreferenceClickListener {
            ChangeEmailDialog().show(parentFragmentManager,"ChangeEmailDialog")
            true
        }

        findPreference<Preference>("change_password")?.setOnPreferenceClickListener {
            ChangePasswordDialog().show(parentFragmentManager, "ChangePasswordDialog")
            true
        }

        //Log out
        findPreference<Preference>("logout")?.setOnPreferenceClickListener {
            showLogoutConfirmationDialog()
            true
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Logout") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear backstack
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show Bottom Navigation again
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }
}
