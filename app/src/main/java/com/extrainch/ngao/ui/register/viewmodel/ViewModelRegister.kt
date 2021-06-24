package com.extrainch.ngao.ui.register.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelRegister : ViewModel() {

    private val _webUrl = MutableLiveData<String>().apply {
        value = "https://www.ngaocredit.com/privacy-policy/"
    }
    val webUrl : LiveData<String> = _webUrl

}