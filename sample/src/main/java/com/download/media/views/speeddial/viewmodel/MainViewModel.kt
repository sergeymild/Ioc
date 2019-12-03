package com.download.media.views.speeddial.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.download.media.views.speeddial.models.SpeedDialModel

open class KViewModel : ViewModel()

class MainViewModel : KViewModel() {
    val favoritesLiveData = MutableLiveData<List<SpeedDialModel>>()
}