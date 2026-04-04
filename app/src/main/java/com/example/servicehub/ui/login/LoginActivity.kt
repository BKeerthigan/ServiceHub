package com.example.servicehub.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.servicehub.ui.home.HomeActivity
import com.example.servicehub.ui.register.RegisterActivity
import com.example.servicehub.viewmodel.LoginUiState
import com.example.servicehub.viewmodel.LoginViewModel

class LoginActivity : ComponentActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(viewModel)
        }
    }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {

    var mobileNumber by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current


    when (state) {
        is LoginUiState.GoHome -> {
            val stateData = state as LoginUiState.GoHome
            LaunchedEffect(stateData) {
                val intent = Intent(context, HomeActivity::class.java)
                intent.putExtra("company_name", stateData.companyName)
                intent.putExtra("contact_name", stateData.contactName)
                intent.putExtra("address", stateData.address)
                context.startActivity(intent)
            }
        }

        is LoginUiState.GoRegister -> {
            val phone = (state as LoginUiState.GoRegister).phone
            LaunchedEffect(phone) {
                val i = Intent(context, RegisterActivity::class.java)
                i.putExtra("phone", phone)
                context.startActivity(i)
            }
        }

        else -> Unit
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(Color.White, shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Login to continue",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your phone number.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } && input.length <= 10) {
                        mobileNumber = input
                    }
                },
                placeholder = { Text("Enter Mobile Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF8D6), shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            val isLoading = state is LoginUiState.Loading
            val enableBtn = mobileNumber.length == 10 && !isLoading

            Button(
                onClick = { viewModel.login(mobileNumber) },
                enabled = enableBtn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Continue", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state is LoginUiState.Error) {
                Text(
                    text = (state as LoginUiState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }
    }
}
