package com.example.simbasfriends.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.simbasfriends.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.models.User

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        initViews()
    }

    private fun initViews() {
        // Forgot Password Click Listener
        binding.loginActivityForgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Login Button Click Listener
        binding.loginActivityLogInButton.setOnClickListener {
            val email = binding.loginActivityEmailTextedit.text.toString().trim()
            val password = binding.loginActivityPasswordTextedit.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Login failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
            }
        }

        // Back Arrow Click Listener
        binding.activityLoginBackArrow.setOnClickListener {
            finish()
        }
    }

    // Show Forgot Password Dialog
    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Password")

        // Create input field for email
        val resetEmail = EditText(this)
        resetEmail.hint = "Enter your email"
        builder.setView(resetEmail)

        // Positive Button (Reset Password)
        builder.setPositiveButton("Reset") { _, _ ->
            val email = resetEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email, this)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_LONG).show()
            }
        }

        // Negative Button (Cancel)
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        // Show Dialog
        builder.show()
    }

    // Send Password Reset Email
    private fun sendPasswordResetEmail(email: String, context: Context) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Password reset email sent", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to send password reset email", Toast.LENGTH_LONG).show()
            }
        }
    }
}
