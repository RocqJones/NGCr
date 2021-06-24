package com.extrainch.ngao.ui.register.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelRegister : ViewModel() {
    private val _webUrlTnC = MutableLiveData<String>().apply {
        value = "https://www.ngaocredit.com/privacy-policy/"
    }

    private val _webUrlPolicy = MutableLiveData<String>().apply {
        value = "https://www.ngaocredit.com/privacy-policy/"
    }
    val webUrlTnC : LiveData<String> = _webUrlTnC
    val webUrlPolicy : LiveData<String> = _webUrlPolicy
}