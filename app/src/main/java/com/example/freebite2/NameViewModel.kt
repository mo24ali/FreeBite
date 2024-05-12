package com.example.freebite2

import androidx.lifecycle.ViewModel

class NameViewModel(fullName: String) : ViewModel() {
    // You can add properties and methods here
    // For example:
    private val _fullName: String = fullName

    fun getFullName(): String {
        return _fullName
    }
}