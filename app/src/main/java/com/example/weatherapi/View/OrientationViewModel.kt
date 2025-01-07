package com.example.weatherapi.View

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class OrientationViewModel : ViewModel() {
    var isPortrait = mutableStateOf(true)

    fun updateOrientation(isPortrait: Boolean) {
        this.isPortrait.value = isPortrait
    }
}
