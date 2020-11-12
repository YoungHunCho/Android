package com.example.rotate_animation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.ImageView

//https://itpangpang.xyz/271
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val iv = findViewById<ImageView>(R.id.iv)
        val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.rotate)
//        iv.animation = animation
        animate(iv, 0.toFloat(), 230.toFloat())
        animate(iv, 230.toFloat(), 100.toFloat())
    }

    private fun animate(view: View, fromDegree: Float, toDegree: Float) {
        val animation: RotateAnimation =     RotateAnimation(fromDegree, toDegree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f)
        animation.duration = 200
        animation.fillAfter = true
        view.startAnimation(animation);
    }

}