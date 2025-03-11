package com.example.simbasfriends.activities

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.simbasfriends.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimation(binding.splashLOTTIELottie)
    }

    private fun startAnimation(lottieAnimationView: LottieAnimationView) {
        lottieAnimationView.resumeAnimation()

        lottieAnimationView.addAnimatorListener(object: Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator) {
                //
            }

            override fun onAnimationEnd(animation: Animator) {
                transactionToWelcome()
            }

            override fun onAnimationCancel(animation: Animator) {
                //
            }

            override fun onAnimationRepeat(animation: Animator) {
                //
            }

        })
    }
    private fun transactionToWelcome(){
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null) startActivity(Intent(this, MainActivity::class.java))
        else startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

}