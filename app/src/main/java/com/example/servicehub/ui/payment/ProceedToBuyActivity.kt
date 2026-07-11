package com.example.servicehub.ui.payment

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.servicehub.data.model.ProceedToBuyData
import com.example.servicehub.session.UserSession
import com.example.servicehub.utils.rememberThrottledClick
import com.example.servicehub.utils.throttledClickable
import com.example.servicehub.ui.register.RegisterActivity
import com.example.servicehub.viewmodel.PaymentMode
import com.example.servicehub.viewmodel.ProceedToBuyUiState
import com.example.servicehub.viewmodel.ProceedToBuyViewModel
import com.example.servicehub.viewmodel.PtbLocStatus
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

private val BRAND_RED = Color(0xFFCC0000)

class ProceedToBuyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cartTotal = intent.getStringExtra("total_amount") ?: ""
        setContent {
            val vm: ProceedToBuyViewModel = viewModel()
            LaunchedEffect(Unit) { vm.load(UserSession.phone) }
            ProceedToBuyScreen(vm = vm, cartTotal = cartTotal, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProceedToBuyScreen(vm: ProceedToBuyViewModel, cartTotal: String, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = androidx.compose.ui.platform.LocalContext.current

    // Redirect to register when check_flag is null
    LaunchedEffect(state.redirectToRegister) {
        if (state.redirectToRegister) {
            ctx.startActivity(
                Intent(ctx, RegisterActivity::class.java)
                    .putExtra("phone", com.example.servicehub.session.UserSession.phone)
            )
            (ctx as? ComponentActivity)?.finish()
        }
    }

    // Navigate to Order Confirmed screen on success, clear payment back stack
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
    state.error?.let { err ->
        val isServerError = err.contains("500") || err.contains("HTTP", ignoreCase = true)
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            title = { Text(if (isServerError) "Server Error" else "Error") },
            text  = {
                Text(
                    if (isServerError)
                        "The server encountered an error. This may be a temporary issue — please try again."
                    else err
                )
            },
            confirmButton = {
                if (isServerError) {
                    TextButton(onClick = {
                        vm.clearError()
                        vm.load(com.example.servicehub.session.UserSession.phone)
                    }) { Text("Retry", color = BRAND_RED) }
                } else {
                    TextButton(onClick = { vm.clearError() }) { Text("OK") }
                }
            },
            dismissButton = if (isServerError) {
                { TextButton(onClick = { vm.clearError() }) { Text("Cancel") } }
            } else null
        )
    }

    // --- GPS setup ---
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(ctx) }
    val locationCallbackRef = remember { mutableStateOf<LocationCallback?>(null) }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK)
            startFetching(fusedClient, vm) { locationCallbackRef.value = it }
        else
            vm.setLocStatus(PtbLocStatus.SERVICES_OFF)
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) checkSettingsThenFetch(ctx, fusedClient, vm, settingsLauncher) { locationCallbackRef.value = it }
        else vm.setLocStatus(PtbLocStatus.PERMISSION_DENIED)
    }

    fun tryStart() {
        if (ctx.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkSettingsThenFetch(ctx, fusedClient, vm, settingsLauncher) { locationCallbackRef.value = it }
        } else {
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
                title = { Text("Proceed to Buy", fontWeight = FontWeight.Bold) },
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
        when {
            state.loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BRAND_RED)
                }
            }
            state.info != null -> {
                val info = state.info!!
                // Only use API amount if it parses to a positive number, otherwise fall back to cartTotal
                val codAmount = info.amount?.toDoubleOrNull()?.takeIf { it > 0.0 }
                    ?.let { if (it % 1.0 == 0.0) it.toLong().toString() else String.format("%.2f", it) }
                    ?: cartTotal
                val qrAmount = info.qrAmount?.takeIf { it > 0.0 }
                    ?.let { if (it % 1.0 == 0.0) it.toLong().toString() else String.format("%.2f", it) }
                    ?: cartTotal
                val displayAmount = when (state.selectedMode) {
                    PaymentMode.COD -> codAmount
                    PaymentMode.QR  -> qrAmount
                }
                PaymentSelectionScreen(
                    state         = state,
                    codAmount     = codAmount,
                    qrAmount      = qrAmount,
                    displayAmount = displayAmount,
                    padding       = padding,
                    onSelectMode  = { vm.selectMode(it) },
                    onPlaceOrder  = {
                        if (state.selectedMode == PaymentMode.COD) {
                            vm.placeCodOrder(UserSession.companyId)
                        } else {
                            ctx.startActivity(
                                Intent(ctx, QrPaymentActivity::class.java)
                                    .putExtra("total_amount", displayAmount)
                            )
                        }
                    }
                )
            }
            !state.loading && state.checkFlag == null && !state.redirectToRegister -> {
                // Still loading or initial state — show spinner
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BRAND_RED)
                }
            }
        }
    }
}

@Composable
private fun ContactAdminScreen(message: String, padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚠️", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text      = message.ifBlank { "Please contact admin to continue." },
                    fontSize  = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color     = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PaymentSelectionScreen(
    state: ProceedToBuyUiState,
    codAmount: String,
    qrAmount: String,
    displayAmount: String,
    padding: PaddingValues,
    onSelectMode: (PaymentMode) -> Unit,
    onPlaceOrder: () -> Unit
) {
    val info = state.info!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Amount card — mode-specific amount passed from parent
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "Select Payment Mode",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF777777),
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Amount : ₹ $displayAmount",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color(0xFF1A1A1A)
                )
            }
        }

        // COD option
        PaymentOptionCard(
            label    = info.cod.orEmpty(),
            notes    = info.codNotes.orEmpty(),
            amount   = codAmount,
            selected = state.selectedMode == PaymentMode.COD,
            onClick  = { onSelectMode(PaymentMode.COD) }
        )

        // QR Code option
        PaymentOptionCard(
            label    = info.qrcode.orEmpty(),
            notes    = info.qrcodeNotes.orEmpty(),
            amount   = qrAmount,
            selected = state.selectedMode == PaymentMode.QR,
            onClick  = { onSelectMode(PaymentMode.QR) }
        )

        // Location status
        LocationStatusRow(locStatus = state.locStatus)

        // Place an Order button
        val canPlace = state.locStatus == PtbLocStatus.OBTAINED && !state.placing
        val onPlaceOrderThrottled = rememberThrottledClick(onPlaceOrder)
        Button(
            onClick  = onPlaceOrderThrottled,
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
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
            }
            Text("Place an Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PaymentOptionCard(
    label: String,
    notes: String,
    amount: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .throttledClickable { onClick() }
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) BRAND_RED else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(14.dp)
            ),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFFFF5F5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (selected) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            RadioButton(
                selected = selected,
                onClick  = onClick,
                colors   = RadioButtonDefaults.colors(selectedColor = BRAND_RED)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(notes, fontSize = 13.sp, color = Color(0xFF666666))
                }
            }
            // Amount shown on the right side of the card
            if (amount.isNotBlank()) {
                Text(
                    text = "₹ $amount",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (selected) BRAND_RED else Color(0xFF1A1A1A)
                )
            }
        }
    }
}

@Composable
private fun LocationStatusRow(locStatus: PtbLocStatus) {
    val (text, color) = when (locStatus) {
        PtbLocStatus.IDLE, PtbLocStatus.FETCHING -> "Detecting location…" to Color(0xFFE65100)
        PtbLocStatus.OBTAINED                    -> "Location ready ✓"  to Color(0xFF2E7D32)
        PtbLocStatus.PERMISSION_DENIED           -> "Location permission denied — tap to open settings" to Color(0xFFB71C1C)
        PtbLocStatus.SERVICES_OFF                -> "Location services are off" to Color(0xFFB71C1C)
        PtbLocStatus.UNAVAILABLE                 -> "Location unavailable — retrying" to Color(0xFFB71C1C)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// ── GPS helpers ──────────────────────────────────────────────────────────────

private fun checkSettingsThenFetch(
    ctx: android.content.Context,
    client: FusedLocationProviderClient,
    vm: ProceedToBuyViewModel,
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
    vm: ProceedToBuyViewModel,
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
