package com.example.servicehub.ui.register

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.servicehub.ui.home.HomeActivity
import com.example.servicehub.viewmodel.PtbLocStatus
import com.example.servicehub.viewmodel.RegisterUiState
import com.example.servicehub.viewmodel.RegisterViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class RegisterActivity : ComponentActivity() {

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phone = intent.getStringExtra("phone") ?: ""

        setContent {
            val state by viewModel.uiState.collectAsState()
            val locStatus by viewModel.locStatus.collectAsStateWithLifecycle()
            val context = LocalContext.current

            if (state is RegisterUiState.Success) {
                val company = (state as RegisterUiState.Success).companyName
                AlertDialog(
                    onDismissRequest = {},
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = { Text("Registration Successful!") },
                    text = { Text("Welcome, $company! Your account has been created successfully.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                val i = Intent(context, HomeActivity::class.java)
                                i.putExtra("company_name", company)
                                context.startActivity(i)
                                finish()
                            }
                        ) {
                            Text("Continue")
                        }
                    }
                )
            }

            // --- GPS setup ---
            val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
            val locationCallbackRef = remember { mutableStateOf<LocationCallback?>(null) }

            val settingsLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK)
                    startFetching(fusedClient, viewModel) { locationCallbackRef.value = it }
                else
                    viewModel.setLocStatus(PtbLocStatus.SERVICES_OFF)
            }

            val permLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) checkSettingsThenFetch(context, fusedClient, viewModel, settingsLauncher) { locationCallbackRef.value = it }
                else viewModel.setLocStatus(PtbLocStatus.PERMISSION_DENIED)
            }

            fun tryStart() {
                if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    checkSettingsThenFetch(context, fusedClient, viewModel, settingsLauncher) { locationCallbackRef.value = it }
                } else {
                    viewModel.setLocStatus(PtbLocStatus.FETCHING)
                    permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

            val lifecycle = LocalLifecycleOwner.current.lifecycle
            DisposableEffect(lifecycle) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) tryStart()
                    if (event == Lifecycle.Event.ON_PAUSE) locationCallbackRef.value?.let { fusedClient.removeLocationUpdates(it) }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }
            // --- GPS end ---

            RegisterScreen(
                phoneNumber = phone,
                isLoading = state is RegisterUiState.Loading,
                errorMessage = (state as? RegisterUiState.Error)?.message,
                locStatus = locStatus,
                onSkip = {
                    val i = Intent(this, HomeActivity::class.java)
                    i.putExtra("company_name", "Guest")
                    startActivity(i)
                    finish()
                },
                onSave = { contactName, shop, address, landmark, city, pincode, salesLead, salesPerson ->
                    viewModel.register(
                        mobile = phone,
                        contactName = contactName,
                        companyNameInput = shop,
                        address = address,
                        landmark = landmark,
                        city = city,
                        pincode = pincode,
                        salesLead = salesLead,
                        salesPerson = salesPerson
                    )
                }
            )
        }
    }
}

private fun checkSettingsThenFetch(
    ctx: android.content.Context,
    client: FusedLocationProviderClient,
    vm: RegisterViewModel,
    settingsLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>,
    onCallback: (LocationCallback) -> Unit
) {
    vm.setLocStatus(PtbLocStatus.FETCHING)
    val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
        .setMaxUpdates(1).build()
    val settingsReq = LocationSettingsRequest.Builder()
        .addLocationRequest(req).setAlwaysShow(true).build()
    LocationServices.getSettingsClient(ctx)
        .checkLocationSettings(settingsReq)
        .addOnSuccessListener { startFetching(client, vm, onCallback) }
        .addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try { settingsLauncher.launch(IntentSenderRequest.Builder(e.resolution).build()) }
                catch (_: Exception) { vm.setLocStatus(PtbLocStatus.SERVICES_OFF) }
            } else {
                vm.setLocStatus(PtbLocStatus.SERVICES_OFF)
            }
        }
}

@SuppressLint("MissingPermission")
private fun startFetching(
    client: FusedLocationProviderClient,
    vm: RegisterViewModel,
    onCallback: (LocationCallback) -> Unit
) {
    client.lastLocation.addOnSuccessListener { loc ->
        if (loc != null) {
            vm.updateLocation(loc.latitude.toString(), loc.longitude.toString())
        } else {
            val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMaxUpdates(3).build()
            val cb = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { l ->
                        vm.updateLocation(l.latitude.toString(), l.longitude.toString())
                        client.removeLocationUpdates(this)
                    }
                }
                override fun onLocationAvailability(avail: LocationAvailability) {
                    if (!avail.isLocationAvailable) vm.setLocStatus(PtbLocStatus.UNAVAILABLE)
                }
            }
            onCallback(cb)
            client.requestLocationUpdates(req, cb, Looper.getMainLooper())
        }
    }.addOnFailureListener { vm.setLocStatus(PtbLocStatus.UNAVAILABLE) }
}
