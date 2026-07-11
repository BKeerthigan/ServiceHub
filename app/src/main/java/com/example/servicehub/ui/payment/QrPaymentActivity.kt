package com.example.servicehub.ui.payment

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.session.UserSession
import com.example.servicehub.viewmodel.PtbLocStatus
import com.example.servicehub.viewmodel.QrPaymentViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.util.Calendar

private val BRAND_RED  = Color(0xFFCC0000)
private val IMG_BASE   = "https://jmsn.in//images//appimage//"

class QrPaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cartTotal = intent.getStringExtra("total_amount") ?: ""
        setContent {
            val vm: QrPaymentViewModel = viewModel()
            LaunchedEffect(Unit) { vm.loadQrCode() }
            QrPaymentScreen(vm = vm, cartTotal = cartTotal, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QrPaymentScreen(vm: QrPaymentViewModel, cartTotal: String, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx   = LocalContext.current

    // Navigate to Order Confirmed on success
    LaunchedEffect(state.orderSuccess) {
        state.orderSuccess?.let { resp ->
            ctx.startActivity(
                Intent(ctx, OrderConfirmedActivity::class.java)
                    .putExtra("sales_order_id",     resp.orderId?.toString() ?: "")
                    .putExtra("sales_order_number", resp.salesOrderNumber ?: "")
                    .putExtra("delivery_date",      resp.deliveryDate ?: "")
            )
            vm.clearSuccess()
            (ctx as? ComponentActivity)?.finish()
        }
    }

    // Error dialog
    state.error?.let {
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            title = { Text("Error") },
            text  = { Text(it) },
            confirmButton = { TextButton(onClick = { vm.clearError() }) { Text("OK") } }
        )
    }

    // --- GPS setup ---
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(ctx) }
    val locationCallbackRef = remember { mutableStateOf<LocationCallback?>(null) }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK)
            startFetchingQr(fusedClient, vm) { locationCallbackRef.value = it }
        else
            vm.setLocStatus(PtbLocStatus.SERVICES_OFF)
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) checkSettingsQr(ctx, fusedClient, vm, settingsLauncher) { locationCallbackRef.value = it }
        else vm.setLocStatus(PtbLocStatus.PERMISSION_DENIED)
    }

    fun tryStart() {
        if (ctx.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            checkSettingsQr(ctx, fusedClient, vm, settingsLauncher) { locationCallbackRef.value = it }
        else {
            vm.setLocStatus(PtbLocStatus.FETCHING)
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) tryStart()
            if (event == Lifecycle.Event.ON_PAUSE)  locationCallbackRef.value?.let { fusedClient.removeLocationUpdates(it) }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    // --- GPS end ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay By QR Code", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BRAND_RED,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF2F2F2)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Amount
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                colors   = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order Total", fontSize = 13.sp, color = Color(0xFF777777))
                    Spacer(Modifier.height(4.dp))
                    Text("₹ $cartTotal", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                }
            }

            // QR Code card
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(14.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.loadingQr) {
                        CircularProgressIndicator(color = BRAND_RED, modifier = Modifier.size(48.dp))
                    } else if (state.qrData != null) {
                        val qr = state.qrData!!
                        if (!qr.address.isNullOrBlank()) {
                            Text(
                                text = "UPI ID: ${qr.address}",
                                fontSize = 13.sp,
                                color = Color(0xFF555555),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                        if (!qr.qrcodeImg.isNullOrBlank()) {
                            val qrUrl = IMG_BASE + qr.qrcodeImg
                            Log.d("QrImage", "Loading QR from: $qrUrl")
                            AsyncImage(
                                model = qrUrl,
                                contentDescription = "QR Code",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(220.dp),
                                onError = { Log.e("QrImage", "Failed to load: $qrUrl — ${it.result.throwable}") },
                                onSuccess = { Log.d("QrImage", "Loaded OK: $qrUrl") }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        Text(
                            "Scan with GPay / PhonePe / Paytm / BHIM",
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }

            // Transaction fields card
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(14.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Transaction Reference No
                    OutlinedTextField(
                        value         = state.transRef,
                        onValueChange = { vm.setTransRef(it) },
                        label         = { Text("Transaction Reference No") },
                        placeholder   = { Text("Enter UTR / Ref No") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp)
                    )

                    // Transaction Date — default is today, displayed as DD-MM-YYYY
                    val calendar = remember { Calendar.getInstance() }
                    // Convert stored YYYY-MM-DD → DD-MM-YYYY for display
                    val displayDate = remember(state.transDate) {
                        val parts = state.transDate.split("-")
                        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else state.transDate
                    }
                    OutlinedTextField(
                        value         = displayDate,
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Transaction Date") },
                        placeholder   = { Text("DD-MM-YYYY") },
                        trailingIcon  = {
                            IconButton(onClick = {
                                DatePickerDialog(
                                    ctx,
                                    { _, y, m, d ->
                                        // Store as YYYY-MM-DD (API format)
                                        vm.setTransDate("$y-${(m+1).toString().padStart(2,'0')}-${d.toString().padStart(2,'0')}")
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = "Pick date", tint = BRAND_RED)
                            }
                        },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Location status
            val (locText, locColor) = when (state.locStatus) {
                PtbLocStatus.IDLE, PtbLocStatus.FETCHING -> "Detecting location…"     to Color(0xFFE65100)
                PtbLocStatus.OBTAINED                    -> "Location ready ✓"        to Color(0xFF2E7D32)
                PtbLocStatus.PERMISSION_DENIED           -> "Location permission denied" to Color(0xFFB71C1C)
                PtbLocStatus.SERVICES_OFF                -> "Location services are off"  to Color(0xFFB71C1C)
                PtbLocStatus.UNAVAILABLE                 -> "Location unavailable"       to Color(0xFFB71C1C)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(locColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = locColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(locText, fontSize = 13.sp, color = locColor, fontWeight = FontWeight.Medium)
            }

            // Place Order button
            val canPlace = state.locStatus == PtbLocStatus.OBTAINED &&
                    state.transRef.isNotBlank() &&
                    state.transDate.isNotBlank() &&
                    !state.placing

            Button(
                onClick  = { vm.placeOrder(UserSession.companyId) },
                enabled  = canPlace,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = BRAND_RED,
                    contentColor   = Color.White,
                    disabledContainerColor = Color(0xFFDDDDDD),
                    disabledContentColor   = Color(0xFF888888)
                )
            ) {
                if (state.placing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text("Place an Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// GPS helpers
private fun checkSettingsQr(
    ctx: android.content.Context,
    client: FusedLocationProviderClient,
    vm: QrPaymentViewModel,
    settingsLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>,
    onCallback: (LocationCallback) -> Unit
) {
    vm.setLocStatus(PtbLocStatus.FETCHING)
    val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).setMaxUpdates(1).build()
    val settingsReq = LocationSettingsRequest.Builder().addLocationRequest(req).setAlwaysShow(true).build()
    LocationServices.getSettingsClient(ctx).checkLocationSettings(settingsReq)
        .addOnSuccessListener { startFetchingQr(client, vm, onCallback) }
        .addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try { settingsLauncher.launch(IntentSenderRequest.Builder(e.resolution).build()) }
                catch (_: Exception) { vm.setLocStatus(PtbLocStatus.SERVICES_OFF) }
            } else vm.setLocStatus(PtbLocStatus.SERVICES_OFF)
        }
}

@SuppressLint("MissingPermission")
private fun startFetchingQr(
    client: FusedLocationProviderClient,
    vm: QrPaymentViewModel,
    onCallback: (LocationCallback) -> Unit
) {
    client.lastLocation.addOnSuccessListener { loc ->
        if (loc != null) {
            vm.updateLocation(loc.latitude.toString(), loc.longitude.toString())
        } else {
            val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).setMaxUpdates(3).build()
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
