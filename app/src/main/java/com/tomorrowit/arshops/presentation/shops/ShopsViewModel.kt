package com.tomorrowit.arshops.presentation.shops

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomorrowit.arshops.location.GetLocationUseCase
import com.tomorrowit.arshops.location.GetNearbyPlacesUseCase
import com.tomorrowit.arshops.model.NearbyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

private val TAG: String = ShopsViewModel::class.java.simpleName

@HiltViewModel
class ShopsViewModel @Inject constructor(
    private val getLocationUseCase: GetLocationUseCase,
    private val getNearbyPlacesUseCase: GetNearbyPlacesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ShopState>(ShopState.Init)
    val state: StateFlow<ShopState> get() = _state

    private lateinit var firstLocation: Location

    init {
        startSearch()
    }

    fun startSearch() {
        _state.value = ShopState.IsLoadingLocation

        viewModelScope.launch {
            firstLocation = getLocationUseCase.invoke(1000L, 0F).first()
            loadShops()
        }
    }

    fun loadShops() {
        _state.value = ShopState.IsLoadingShops

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response =
                    getNearbyPlacesUseCase.invoke(firstLocation.latitude, firstLocation.longitude)
                _state.value = ShopState.SuccessShops(response)
            } catch (e: Exception) {
                _state.value = ShopState.ErrorShops(e.message ?: "Unknown error.")
            }
        }
    }

    fun getLocationUpdates(): Flow<Location> {
        return getLocationUseCase.invoke(1000L, 0F)
    }
}

sealed class ShopState {
    data object Init : ShopState()
    data object IsLoadingLocation : ShopState()
    data object LocationReady : ShopState()
    data object IsLoadingShops : ShopState()
    data class SuccessShops(val list: List<NearbyStore>) : ShopState()
    data class ErrorShops(val error: String) : ShopState()
}