package com.example.servicehub.ui.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.servicehub.ui.home.HomeActivity
import com.example.servicehub.viewmodel.RegisterUiState
import com.example.servicehub.viewmodel.RegisterViewModel

class RegisterActivity : ComponentActivity() {

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phone = intent.getStringExtra("phone") ?: ""

        setContent {
            val state by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            // ✅ Navigate when Success
            if (state is RegisterUiState.Success) {
                val company = (state as RegisterUiState.Success).companyName
                LaunchedEffect(company) {
                    val i = Intent(context, HomeActivity::class.java)
                    i.putExtra("company_name", company)
                    context.startActivity(i)
                    finish()
                }
            }

            RegisterScreen(
                phoneNumber = phone,
                isLoading = state is RegisterUiState.Loading,
                errorMessage = (state as? RegisterUiState.Error)?.message,
                onSkip = {
                    val i = Intent(this, HomeActivity::class.java)
                    i.putExtra("company_name", "Guest")
                    startActivity(i)
                    finish()
                },
                onSave = { contactName, shop, address, landmark, city, pincode ->
                    viewModel.register(
                        mobile = phone,
                        contactName = contactName,
                        companyNameInput = shop,
                        address = address,
                        landmark = landmark,
                        city = city,
                        pincode = pincode
                    )
                }
            )
        }
    }
}
