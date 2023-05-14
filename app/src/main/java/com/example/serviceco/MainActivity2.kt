package com.example.serviceco

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.airbnb.lottie.LottieAnimationView

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide();
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.background)))
        Handler().postDelayed({
            val intent = Intent(this@MainActivity2, MainActivity::class.java)
            startActivity(intent)

            finish()
        },6000)
    }
}