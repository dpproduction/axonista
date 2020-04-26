package com.dgroup.testappaxonista

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream


class MainViewModel : ViewModel() {

    val resultData = MutableLiveData<Resource<File>>()

    fun calcConferenceDates(inputStream: InputStream, filesDir: File) {
        viewModelScope.launch {
            try {
                val file = CalcConferenceDatesUseCase().execute(inputStream, filesDir)
                resultData.postValue(Resource(file))
            } catch (e: Exception) {
                e.printStackTrace()
                resultData.postValue(Resource(throwable = e))
            }
        }
    }

}