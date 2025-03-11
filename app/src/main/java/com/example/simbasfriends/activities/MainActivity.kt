package com.example.simbasfriends.activities

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.simbasfriends.R
import com.example.simbasfriends.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root) // Ensure view is set before finding NavController

    val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
        ?.let { fragment -> androidx.navigation.fragment.NavHostFragment.findNavController(fragment) }

    if (navController != null) {
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_events,
                R.id.navigation_new_event,
                R.id.navigation_home,
                R.id.navigation_profile,
                R.id.navigation_friends
            )
        )
        binding.navView.setupWithNavController(navController)
    } else {
        throw IllegalStateException("NavController is null. Check if nav_host_fragment_activity_main exists in activity_main.xml.")
    }
}

}