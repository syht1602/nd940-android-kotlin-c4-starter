package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.base.BaseViewModel

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationViewModel(application: Application) : BaseViewModel(application) {
    //    val authStateEvent = SingleLiveEvent<Boolean>()
    private val _isNavigateToLogin = MutableLiveData<Boolean?>()
    val isLoginBtnClick: LiveData<Boolean?>
        get() = _isNavigateToLogin

    init {
        getLoginState()
    }

    fun onLoginClick() {
        _isNavigateToLogin.value = true
    }

    fun onLoginClickCompleted() {
        _isNavigateToLogin.value = null
    }
}