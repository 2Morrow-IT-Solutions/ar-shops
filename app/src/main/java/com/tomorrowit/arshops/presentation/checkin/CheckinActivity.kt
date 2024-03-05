package com.tomorrowit.arshops.presentation.checkin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomorrowit.arshops.databinding.ActivityCheckinBinding

class CheckinActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckinBinding
    private lateinit var shopId: String

    companion object {
        const val SHOP_ID: String = "SHOP_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shopId = intent.getStringExtra(SHOP_ID) ?: ""

        binding.activityCheckinContent.text = shopId
    }
}