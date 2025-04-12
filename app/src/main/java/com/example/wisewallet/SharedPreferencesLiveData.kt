package com.example.wisewallet

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

class SharedPreferencesLiveData(
    private val sharedPreferences: SharedPreferences,
    private val key: String
) : LiveData<String>() {
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
        if (changedKey == key) {
            value = sharedPreferences.getString(key, null)
        }
    }

    override fun onActive() {
        super.onActive()
        value = sharedPreferences.getString(key, null)
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onInactive() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        super.onInactive()
    }
}