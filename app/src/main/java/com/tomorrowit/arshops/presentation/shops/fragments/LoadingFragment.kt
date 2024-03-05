package com.tomorrowit.arshops.presentation.shops.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.tomorrowit.arshops.R
import com.tomorrowit.arshops.databinding.FragmentLoadingBinding
import com.tomorrowit.arshops.presentation.shops.ShopState
import com.tomorrowit.arshops.presentation.shops.ShopsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class LoadingFragment : Fragment() {
    private lateinit var binding: FragmentLoadingBinding

    private val viewModel: ShopsViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentLoadingBinding.inflate(inflater, container, false)
        .apply { binding = this }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragmentLoadingRetry.setOnClickListener {
            viewModel.loadShops()
        }
    }

    override fun onStart() {
        super.onStart()
        observeState()
    }

    private fun observeState() {
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when (state) {
                is ShopState.Init -> {}
                is ShopState.ErrorShops -> {
                    withContext(Dispatchers.Main) {
                        binding.fragmentLoadingStatus.text = getString(R.string.error_loading_shops)
                        binding.fragmentLoadingRetry.visibility = View.VISIBLE
                    }
                }

                is ShopState.IsLoadingLocation -> {
                    withContext(Dispatchers.Main) {
                        binding.fragmentLoadingStatus.text =
                            getString(R.string.loading_user_location)
                    }
                }

                is ShopState.IsLoadingShops -> {
                    withContext(Dispatchers.Main) {
                        binding.fragmentLoadingStatus.text = getString(R.string.loading_near_shops)
                    }
                }

                is ShopState.LocationReady -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoadingFragment.requireContext(),
                            "Locations ready",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is ShopState.SuccessShops -> {

                }
            }
        }.launchIn(lifecycleScope)
    }
}