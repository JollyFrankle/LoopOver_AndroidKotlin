package com.example.poolover_jinston

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.poolover_jinston.databinding.ActivityMainMenuBinding

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn2x2.setOnClickListener {
            sendToGameActivity(2)
        }

        binding.btn3x3.setOnClickListener {
            sendToGameActivity(3)
        }

        binding.btn4x4.setOnClickListener {
            sendToGameActivity(4)
        }

        binding.btn5x5.setOnClickListener {
            sendToGameActivity(5)
        }

        binding.btn6x6.setOnClickListener {
            sendToGameActivity(6)
        }

        binding.btn7x7.setOnClickListener {
            sendToGameActivity(7)
        }

        binding.btn8x8.setOnClickListener {
            sendToGameActivity(8)
        }

        binding.btn9x9.setOnClickListener {
            sendToGameActivity(9)
        }

        binding.btn10x10.setOnClickListener {
            sendToGameActivity(10)
        }

        binding.btn20x20.setOnClickListener {
            sendToGameActivity(20)
        }
    }

    private fun sendToGameActivity(tileCount: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("tileCount", tileCount)
        startActivity(intent)
    }
}