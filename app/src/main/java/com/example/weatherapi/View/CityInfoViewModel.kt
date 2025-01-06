package com.example.weatherapi.View

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapi.Model.CityInfo

class CityInfoViewModel : ViewModel() {
    // MutableLiveData pour stocker la ville
    private val _cityInfo = MutableLiveData<CityInfo?>()

    // Getter pour accéder à la valeur CityInfo sans exposer la MutableLiveData
    val cityInfo: CityInfo?
        get() = _cityInfo.value

    // Setter pour mettre à jour la ville
    fun setCityInfo(city: CityInfo?) {
        _cityInfo.value = city
    }
}
