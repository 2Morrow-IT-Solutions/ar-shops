package com.tomorrowit.arshops.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tomorrowit.arshops.R
import com.tomorrowit.arshops.databinding.ActivityMainBinding
import com.tomorrowit.arshops.presentation.shops.ShopsActivity


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.openAr.setOnClickListener {
            val myIntent: Intent = Intent(
                this@MainActivity,
                ShopsActivity::class.java
            )
            this@MainActivity.startActivity(myIntent)
        }
    }
}