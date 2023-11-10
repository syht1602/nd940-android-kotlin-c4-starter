package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private val viewModel: AuthenticationViewModel by viewModel()
    private lateinit var binding: ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        // TODO: Implement the create account and sign in using FirebaseUI,
        //  use sign in using email and sign in using Google

        // TODO: If the user was authenticated, send him to RemindersActivity

        // TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
        binding.viewModel = this.viewModel
        with(viewModel) {
            isLoginBtnClick.observe(this@AuthenticationActivity) {
                it?.let {
                    if (it) {
                        doLogin()
                        onLoginClickCompleted()
                    }
                }
            }
        }
        setContentView(binding.root)
    }

    private var launcher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.getLoginState()
                val intent = Intent(this, RemindersActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Log.e("AuthenticationActivity", "Login failed")
            }
        }

    private fun doLogin() {
        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )
        //Default theme will always set as dark mode with black background
        //This is an issue have mentioned here https://github.com/firebase/FirebaseUI-Android/issues/2130
        //Change to default app theme cannot show the edittext field when using white background
        //so I have customized a bit for showing the email field
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setLogo(R.drawable.login_reminder)
            .setAvailableProviders(providers)
            .setTheme(R.style.FirebaseUI)
            .build()
        launcher.launch(signInIntent)
    }
}