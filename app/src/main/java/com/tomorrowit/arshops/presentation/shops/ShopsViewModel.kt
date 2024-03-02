package com.tomorrowit.arshops.presentation.shops

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomorrowit.arshops.location.GetLocationUseCase
import com.tomorrowit.arshops.location.GetNearbyPlacesUseCase
import com.tomorrowit.arshops.model.NearbyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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

    private var firstLocation = true

    init {
        viewModelScope.launch {
            getLocationUseCase.invoke(1000L, 0F)
                .onStart {
                    /* Handle the start of the flow */
                    Log.i(TAG, "getLocationUseCase onStart")
                }
                .onEach { location ->
                    /* Handle each emitted location */
                    if (firstLocation) {
                        _state.value =
                            ShopState.UpdateLocation(location.latitude, location.longitude)
                        getNearbyPlaces(location.latitude, location.longitude)
                        firstLocation = false
                    }
                }
                .catch { exception ->
                    /* Handle any exception that occurs */
                    ShopState.ErrorShops(exception.message ?: "Unknown error.")
                }
                .onCompletion {
                    /* Handle the completion of the flow */
                    Log.i(TAG, "getLocationUseCase onCompletion")
                }
                .launchIn(this)
        }
    }

    private fun getNearbyPlaces(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = getNearbyPlacesUseCase.invoke(latitude, longitude)
                _state.value = ShopState.SuccessShops(response)
            } catch (e: Exception) {
                _state.value = ShopState.ErrorShops(e.message ?: "Unknown error.")
            }
        }
    }
}

sealed class ShopState {
    data object Init : ShopState()
    data class UpdateLocation(val lat: Double, val lng: Double) : ShopState()
    data class SuccessShops(val list: List<NearbyStore>) : ShopState()
    data class ErrorShops(val error: String) : ShopState()
}