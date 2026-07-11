package com.example.servicehub.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.servicehub.utils.rememberThrottledClick
import com.example.servicehub.viewmodel.PtbLocStatus

private val LEAD_PERSON_OPTIONS = (1..9).map { it.toString() }

@Composable
fun RegisterScreen(
    phoneNumber: String,
    isLoading: Boolean,
    errorMessage: String?,
    locStatus: PtbLocStatus,
    onSkip: () -> Unit,
    onSave: (contactName: String, shopName: String, address: String, landmark: String, city: String, pincode: String, salesLead: Int, salesPerson: Int) -> Unit
) {
    var contactName by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var salesLead by remember { mutableStateOf(1) }
    var salesPerson by remember { mutableStateOf(1) }

    val isSaveEnabled = remember(contactName, shopName, address, city, pincode, isLoading, locStatus) {
        contactName.trim().isNotEmpty() &&
                shopName.trim().isNotEmpty() &&
                address.trim().isNotEmpty() &&
                city.trim().isNotEmpty() &&
                pincode.length == 6 &&
                pincode.all { it.isDigit() } &&
                locStatus == PtbLocStatus.OBTAINED &&
                !isLoading
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
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

            Spacer(modifier = Modifier.height(12.dp))

            NumberDropdown(
                label = "Sales Lead",
                selected = salesLead,
                options = LEAD_PERSON_OPTIONS,
                onSelect = { salesLead = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NumberDropdown(
                label = "Salesperson",
                selected = salesPerson,
                options = LEAD_PERSON_OPTIONS,
                onSelect = { salesPerson = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location status row
            val (locText, locColor) = when (locStatus) {
                PtbLocStatus.IDLE, PtbLocStatus.FETCHING -> "Detecting location…" to Color(0xFFE65100)
                PtbLocStatus.OBTAINED                    -> "Location ready ✓"   to Color(0xFF2E7D32)
                PtbLocStatus.PERMISSION_DENIED           -> "Location permission denied" to Color(0xFFB71C1C)
                PtbLocStatus.SERVICES_OFF                -> "Location services are off"  to Color(0xFFB71C1C)
                PtbLocStatus.UNAVAILABLE                 -> "Location unavailable — retrying" to Color(0xFFB71C1C)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(locColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = locColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(locText, fontSize = 13.sp, color = locColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button with Progress
            val onSaveClick = rememberThrottledClick {
                onSave(contactName.trim(), shopName.trim(), address.trim(), landmark.trim(), city.trim(), pincode.trim(), salesLead, salesPerson)
            }
            Button(
                onClick = onSaveClick,
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
        } // inner white card Column
        } // outer scroll Column
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberDropdown(
    label: String,
    selected: Int,
    options: List<String>,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.toString(),
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(label) },
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .background(Color(0xFFFFF8D6), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option.toInt())
                        expanded = false
                    }
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
