package com.example.servicehub.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(
    phoneNumber: String,
    isLoading: Boolean,
    errorMessage: String?,
    onSkip: () -> Unit,
    onSave: ( contactName: String,shopName: String, address: String, landmark: String, city: String, pincode: String) -> Unit
) {
    var contactName by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }

    // ✅ Save enabled logic (THIS is why your button was disabled)
    val isSaveEnabled = remember(shopName, address, city, pincode, isLoading) {
        contactName.trim().isNotEmpty() &&
                shopName.trim().isNotEmpty() &&
                address.trim().isNotEmpty() &&
                city.trim().isNotEmpty() &&
                pincode.length == 6 &&
                pincode.all { it.isDigit() } &&
                !isLoading
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {

            // Title + Skip
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Registration",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onSkip, enabled = !isLoading) {
                    Text("Skip")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone (read-only)
            AppField(
                value = phoneNumber,
                onValueChange = {},
                hint = "Phone Number",
                enabled = false
            )

            Spacer(modifier = Modifier.height(12.dp))
            AppField(
                value = contactName,
                onValueChange = { contactName = it },
                hint = "Name"
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppField(
                value = shopName,
                onValueChange = { shopName = it },
                hint = "Shop Name"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Address (multi-line)
            AppField(
                value = address,
                onValueChange = { address = it },
                hint = "Address",
                singleLine = false,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppField(
                value = landmark,
                onValueChange = { landmark = it },
                hint = "Landmark"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppField(
                value = city,
                onValueChange = { city = it },
                hint = "City"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pincode: only digits + max 6
            AppField(
                value = pincode,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } && input.length <= 6) {
                        pincode = input
                    }
                },
                hint = "Pin code",
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button with Progress
            Button(
                onClick = {
                    onSave(contactName.trim(),shopName.trim(), address.trim(), landmark.trim(), city.trim(), pincode.trim())
                },
                enabled = isSaveEnabled,
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
                    Text("Save", color = Color.White, fontSize = 18.sp)
                }
            }

            // Error message
            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AppField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(hint) },
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF8D6), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines
    )
}
