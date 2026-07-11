package com.example.servicehub.ui.account

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.servicehub.session.UserSession
import com.example.servicehub.utils.rememberThrottledClick
import com.example.servicehub.viewmodel.AccountSettingsViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

private val EA_RED      = Color(0xFFCC0000)
private val EA_FIELD_BG = Color(0xFFFFFDE7)

private enum class LocStatus {
    IDLE,           // not started yet
    FETCHING,       // waiting for fix
    OBTAINED,       // ✅ got lat/lng
    PERMISSION_DENIED,  // user denied location permission
    SERVICES_OFF,   // location toggle is OFF (not fetching yet)
    UNAVAILABLE     // services on + permission ok but still no fix
}

class EditAddressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contactName = intent.getStringExtra("contact_name").orEmpty()
        val companyName = intent.getStringExtra("company_name").orEmpty()
        val address     = intent.getStringExtra("address").orEmpty()
        val landmark    = intent.getStringExtra("landmark").orEmpty()
        val city        = intent.getStringExtra("city").orEmpty()
        val pincode     = intent.getStringExtra("pincode").orEmpty()

        setContent {
            val vm: AccountSettingsViewModel = viewModel()
            // If no extras passed, load from API so fields are pre-populated
            LaunchedEffect(Unit) {
                if (address.isBlank()) vm.load(UserSession.companyId)
            }
            val vmState by vm.state.collectAsStateWithLifecycle()
            // Use intent extras if provided, otherwise fall back to API data
            val resolvedContact = contactName.ifBlank { vmState.details?.contact_name.orEmpty() }
            val resolvedCompany = companyName.ifBlank { vmState.details?.company_name.orEmpty() }
            val resolvedAddress = address.ifBlank { vmState.details?.address.orEmpty() }
            val resolvedLandmark = landmark.ifBlank { vmState.details?.landmark.orEmpty() }
            val resolvedCity = city.ifBlank { vmState.details?.city.orEmpty() }
            val resolvedPincode = pincode.ifBlank { vmState.details?.pincode.orEmpty() }

            EditAddressScreen(
                vm           = vm,
                companyId    = UserSession.companyId,
                contactName  = resolvedContact,
                companyName  = resolvedCompany,
                initAddress  = resolvedAddress,
                initLandmark = resolvedLandmark,
                initCity     = resolvedCity,
                initPincode  = resolvedPincode,
                onBack       = { finish() },
                onSaved      = {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAddressScreen(
    vm: AccountSettingsViewModel,
    companyId: String,
    contactName: String,
    companyName: String,
    initAddress: String,
    initLandmark: String,
    initCity: String,
    initPincode: String,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state       by vm.state.collectAsStateWithLifecycle()
    val ctx         = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(ctx) }

    var address   by remember { mutableStateOf(initAddress) }
    var landmark  by remember { mutableStateOf(initLandmark) }
    var city      by remember { mutableStateOf(initCity) }
    var pincode   by remember { mutableStateOf(initPincode) }

    // When extras are blank, initAddress etc. arrive async from the API load.
    // remember only initialises once, so we sync here when the value first becomes non-blank.
    LaunchedEffect(initAddress)  { if (address.isBlank()  && initAddress.isNotBlank())  address  = initAddress  }
    LaunchedEffect(initLandmark) { if (landmark.isBlank() && initLandmark.isNotBlank()) landmark = initLandmark }
    LaunchedEffect(initCity)     { if (city.isBlank()     && initCity.isNotBlank())     city     = initCity     }
    LaunchedEffect(initPincode)  { if (pincode.isBlank()  && initPincode.isNotBlank())  pincode  = initPincode  }
    var latitude  by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var locStatus by remember { mutableStateOf(LocStatus.IDLE) }
    var saving    by remember { mutableStateOf(false) }

    val canSave = address.isNotBlank() && landmark.isNotBlank() &&
                  city.isNotBlank()    && pincode.isNotBlank()  &&
                  locStatus == LocStatus.OBTAINED && !saving

    // ── LocationCallback — receives fixes from requestLocationUpdates ───────
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    latitude  = loc.latitude.toString()
                    longitude = loc.longitude.toString()
                    locStatus = LocStatus.OBTAINED
                    fusedClient.removeLocationUpdates(this)
                }
            }
        }
    }

    // Cleanup when screen leaves composition
    DisposableEffect(Unit) {
        onDispose { fusedClient.removeLocationUpdates(locationCallback) }
    }

    // ── Step 2: actually fetch the fix (called only after settings are OK) ─
    fun startFetching() {
        locStatus = LocStatus.FETCHING
        try {
            // Try cached location first — zero latency
            fusedClient.lastLocation.addOnSuccessListener { last ->
                if (last != null) {
                    latitude  = last.latitude.toString()
                    longitude = last.longitude.toString()
                    locStatus = LocStatus.OBTAINED
                } else {
                    // No cache — start live updates (network + GPS)
                    val req = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 2000L)
                        .setMinUpdateIntervalMillis(1000L)
                        .setMaxUpdates(1)
                        .build()
                    try {
                        fusedClient.requestLocationUpdates(req, locationCallback, android.os.Looper.getMainLooper())
                            .addOnFailureListener { locStatus = LocStatus.UNAVAILABLE }
                    } catch (_: SecurityException) { locStatus = LocStatus.PERMISSION_DENIED }
                }
            }.addOnFailureListener { locStatus = LocStatus.UNAVAILABLE }
        } catch (_: SecurityException) { locStatus = LocStatus.PERMISSION_DENIED }
    }

    // ── Step 1b: system dialog result — user tapped OK/Cancel on "Turn on location?" ─
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startFetching()           // user turned on location ✅
        } else {
            locStatus = LocStatus.SERVICES_OFF  // user dismissed the dialog
        }
    }

    // ── Step 1: check if location SERVICES are enabled, prompt if not ───────
    fun checkSettingsThenFetch() {
        locStatus = LocStatus.FETCHING
        val locationReq = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 2000L).build()
        val settingsReq = LocationSettingsRequest.Builder()
            .addLocationRequest(locationReq)
            .setAlwaysShow(true)   // always show the dialog, even if user dismissed it before
            .build()

        LocationServices.getSettingsClient(ctx)
            .checkLocationSettings(settingsReq)
            .addOnSuccessListener {
                // Location services are ON → go fetch
                startFetching()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // Location services are OFF → show native "Turn on location?" dialog
                    try {
                        settingsLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                        )
                    } catch (_: Exception) { locStatus = LocStatus.SERVICES_OFF }
                } else {
                    locStatus = LocStatus.UNAVAILABLE
                }
            }
    }

    // ── Permission launcher ─────────────────────────────────────────────────
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) checkSettingsThenFetch() else locStatus = LocStatus.PERMISSION_DENIED
    }

    // ── Entry point: called on resume, after returning from any settings screen
    fun tryStart() {
        if (locStatus == LocStatus.OBTAINED) return   // already have a fix, don't redo

        val fine   = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            checkSettingsThenFetch()
        } else {
            locStatus = LocStatus.FETCHING   // show spinner while dialog is open
            permLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    // Run on every ON_RESUME — handles first open + return from permission/location settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) tryStart()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Save result handlers
    LaunchedEffect(state.saveResult) {
        state.saveResult?.let {
            saving = false
            Toast.makeText(ctx, "Address updated successfully", Toast.LENGTH_SHORT).show()
            vm.clearSaveState()
            onSaved()
        }
    }
    LaunchedEffect(state.saveError) {
        state.saveError?.let {
            saving = false
            Toast.makeText(ctx, "Error: $it", Toast.LENGTH_SHORT).show()
            vm.clearSaveState()
        }
    }

    // ── UI ──────────────────────────────────────────────────────────────────
    Scaffold(containerColor = Color.White) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1A1A1A))
                }
                Spacer(Modifier.width(4.dp))
                Text("Edit Address", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF1A1A1A))
            }

            Spacer(Modifier.height(4.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddressField(value = contactName, label = "Name",      enabled = false, onChange = {})
                AddressField(value = companyName, label = "Shop Name", enabled = false, onChange = {})
                AddressField(value = address,  label = "Address",  enabled = true, onChange = { address = it },  imeAction = ImeAction.Next)
                AddressField(value = landmark, label = "Landmark", enabled = true, onChange = { landmark = it }, imeAction = ImeAction.Next)
                AddressField(value = city,     label = "City",     enabled = true, onChange = { city = it },     imeAction = ImeAction.Next)
                AddressField(value = pincode,  label = "Pin code", enabled = true, onChange = { pincode = it },  keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)

                LocationStatusRow(
                    status     = locStatus,
                    onRetry    = { tryStart() },
                    onEnableLocation = { checkSettingsThenFetch() },
                    onOpenAppSettings = {
                        ctx.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", ctx.packageName, null)
                            }
                        )
                    }
                )

                Spacer(Modifier.height(4.dp))

                val onSave = rememberThrottledClick {
                    if (canSave) {
                        saving = true
                        vm.editAddress(
                            companyId = companyId,
                            address   = address,
                            landmark  = landmark,
                            city      = city,
                            pincode   = pincode,
                            latitude  = latitude,
                            longitude = longitude
                        )
                    }
                }
                Button(
                    onClick = onSave,
                    enabled  = canSave,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(28.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = EA_RED,
                        disabledContainerColor = Color(0xFFCCCCCC)
                    )
                ) {
                    if (saving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LocationStatusRow(
    status: LocStatus,
    onRetry: () -> Unit,
    onEnableLocation: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    val (bgColor, iconTint, message) = when (status) {
        LocStatus.IDLE             -> Triple(Color(0xFFF5F5F5), Color.Gray,        "Checking location…")
        LocStatus.FETCHING         -> Triple(Color(0xFFFFF8E1), Color(0xFFF57C00), "Detecting location…")
        LocStatus.OBTAINED         -> Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "Location obtained ✓")
        LocStatus.SERVICES_OFF     -> Triple(Color(0xFFFFEBEE), EA_RED,            "Location is turned off")
        LocStatus.PERMISSION_DENIED-> Triple(Color(0xFFFFEBEE), EA_RED,            "Location permission denied")
        LocStatus.UNAVAILABLE      -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Couldn't get location")
    }

    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (status == LocStatus.FETCHING) {
                CircularProgressIndicator(color = iconTint, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(message, fontSize = 13.sp, color = iconTint, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))

            when (status) {
                LocStatus.SERVICES_OFF -> {
                    TextButton(
                        onClick        = onEnableLocation,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Turn On", fontSize = 12.sp, color = EA_RED, fontWeight = FontWeight.Bold)
                    }
                }
                LocStatus.PERMISSION_DENIED -> {
                    TextButton(
                        onClick        = onOpenAppSettings,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Open Settings", fontSize = 12.sp, color = EA_RED, fontWeight = FontWeight.Bold)
                    }
                }
                LocStatus.UNAVAILABLE -> {
                    TextButton(
                        onClick        = onRetry,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = iconTint, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Retry", fontSize = 12.sp, color = iconTint, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun AddressField(
    value: String,
    label: String,
    enabled: Boolean,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    val focusManager = LocalFocusManager.current
    val bgColor = if (enabled) EA_FIELD_BG else Color(0xFFF0F0F0)

    TextField(
        value           = value,
        onValueChange   = onChange,
        enabled         = enabled,
        placeholder     = { Text(label, color = Color(0xFFAAAAAA)) },
        singleLine      = true,
        shape           = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType   = keyboardType,
            imeAction      = imeAction,
            capitalization = if (keyboardType == KeyboardType.Number)
                                 KeyboardCapitalization.None
                             else KeyboardCapitalization.Sentences
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { focusManager.clearFocus() }
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor    = bgColor,
            unfocusedContainerColor  = bgColor,
            disabledContainerColor   = bgColor,
            focusedIndicatorColor    = Color.Transparent,
            unfocusedIndicatorColor  = Color.Transparent,
            disabledIndicatorColor   = Color.Transparent,
            focusedTextColor         = Color(0xFF1A1A1A),
            unfocusedTextColor       = Color(0xFF1A1A1A),
            disabledTextColor        = Color(0xFF888888),
            cursorColor              = EA_RED
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
