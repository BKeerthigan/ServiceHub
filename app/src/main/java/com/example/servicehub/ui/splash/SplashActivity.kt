package com.example.servicehub.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.servicehub.session.UserSession
import com.example.servicehub.ui.home.HomeActivity
import com.example.servicehub.ui.login.LoginActivity

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

public class SplashActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?,) {
        super.onCreate(savedInstanceState)
        setContent {
            WelcomeScreen()
        }

        lifecycleScope.launch {
            delay(3000)
            // If the user was already logged in (flag "0" or "1"), go straight to Home.
            // Only send to Login when there is no saved session (flag is null).
            val destination = if (UserSession.loginFlag != null) HomeActivity::class.java
                              else LoginActivity::class.java
            startActivity(Intent(this@SplashActivity, destination))
            finish()
        }
    }

    @Composable
    fun WelcomeScreen(){
        Column(
            modifier = Modifier.Companion.fillMaxSize().background(Color(0xFFFFA726)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {

            Text(text = "Sakthi Corp", fontSize = 28.sp, color = Color.Companion.White)

        }
    }


}