package com.ilatyphi95.microlytxphoneinfo.ui.mainactivity

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ilatyphi95.microlytxphoneinfo.data.ItemUtils
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class MainActivityViewModelFactory(private val application: Application, private val itemUtils: ItemUtils)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(application, itemUtils) as T
        }
        throw IllegalArgumentException("Unknown viewmodel")
    }
}