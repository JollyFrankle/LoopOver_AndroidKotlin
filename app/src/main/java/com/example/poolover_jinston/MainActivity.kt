package com.example.poolover_jinston

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.poolover_jinston.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var startTime = 0L
    private var timer: CountDownTimer? = null
    private var movesCount = 0

    private lateinit var gameFragment: GameFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set game fragment
        gameFragment = GameFragment()
        supportFragmentManager.beginTransaction()
            .replace(binding.boardFragment.id, gameFragment)
            .commit()

        binding.btnScramble.setOnClickListener {
            scramble()
        }

        binding.btnOpenVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com/watch?v=95rtiz-V2zM"))
            startActivity(intent)
        }
    }

    fun setupTimer() {
        val timerTv = binding.tvTimer
        // setup stopwatch
        val stopwatch = object : CountDownTimer(1000000000, 10) {
            override fun onTick(millisUntilFinished: Long) {
                val ms = (System.currentTimeMillis() - startTime) % 1000 / 10
                val seconds = (System.currentTimeMillis() - startTime) / 1000
                val minutes = seconds / 60
                timerTv.text = String.format("%02d:%02d.%02d", minutes, seconds % 60, ms)
            }

            override fun onFinish() {
                timerTv.text = "00:00.00"
            }
        }

        timer = stopwatch
        stopwatch.start()
        movesCount = 0
        binding.tvMovesCount.text = "Moves: $movesCount"
    }



    fun scramble() {
        gameFragment.scramble()

        timer?.cancel()
        startTime = 0L
        binding.tvTimer.text = "00:00.00"
        movesCount = 0
        binding.tvMovesCount.text = "Moves: $movesCount"
    }

    fun checkIsSolved(boardContent: List<MutableList<Char>>) {
        val solved = boardContent.flatten() == ('A'..'Y').toList()
        movesCount++
        binding.tvMovesCount.text = "Moves: $movesCount"
        if (solved) {
            Toast.makeText(this, "Solved!", Toast.LENGTH_SHORT).show()
            timer?.cancel()
            startTime = 0L
            movesCount = 0
        }
    }
}