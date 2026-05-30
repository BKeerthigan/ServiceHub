package com.example.servicehub.ui.account

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.servicehub.session.UserSession
import com.example.servicehub.viewmodel.AccountSettingsViewModel

private val YD_RED      = Color(0xFFCC0000)
private val YD_RED_DARK = Color(0xFF990000)
private val YD_BG       = Color(0xFFF4F6F8)

class YourDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AccountSettingsViewModel = viewModel()
            val companyId = UserSession.companyId
            LaunchedEffect(companyId) { vm.load(companyId) }
            YourDetailsScreen(vm = vm, companyId = companyId, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourDetailsScreen(
    vm: AccountSettingsViewModel,
    companyId: String,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    var editField by remember { mutableStateOf<String?>(null) }

    // Reload fresh data every time this screen resumes
    // (handles returning from EditAddressActivity with updated address)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.load(companyId)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.saveResult) {
        state.saveResult?.let { snackbarHostState.showSnackbar(it); vm.clearSaveState() }
    }
    LaunchedEffect(state.saveError) {
        state.saveError?.let { snackbarHostState.showSnackbar("Error: $it"); vm.clearSaveState() }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = YD_BG
    ) { scaffoldPadding ->
        when {
            state.loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = YD_RED)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.error}", color = Color.Red)
                }
            }
            else -> {
                val details = state.details

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Gradient red header with rounded bottom ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(YD_RED_DARK, YD_RED)
                                )
                            )
                            .padding(bottom = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Back button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                            }

                            // Avatar with white border ring
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .border(3.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                    .padding(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(52.dp)
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text       = details?.contact_name.orEmpty().ifBlank { "—" },
                                color      = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 20.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text     = details?.company_name.orEmpty(),
                                color    = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // ── Content ──
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 24.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "PERSONAL INFORMATION",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF888888),
                            modifier   = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )

                        // Name row
                        DetailCard(
                            label       = "Name",
                            value       = details?.contact_name.orEmpty().ifBlank { "—" },
                            icon        = Icons.Filled.Person,
                            iconBgColor = Color(0xFFFFEBEB),
                            iconTint    = YD_RED,
                            editable    = true,
                            onEditClick = { editField = "name" }
                        )

                        // Mobile row
                        DetailCard(
                            label       = "Mobile Number",
                            value       = "+91-${details?.mobile_app.orEmpty()}",
                            icon        = Icons.Filled.Phone,
                            iconBgColor = Color(0xFFE8F5E9),
                            iconTint    = Color(0xFF388E3C),
                            editable    = false
                        )

                        // Address row
                        val addressFull = buildString {
                            append(details?.address.orEmpty())
                            if (!details?.landmark.isNullOrBlank()) append(", ${details?.landmark}")
                            if (!details?.city.isNullOrBlank()) append(", ${details?.city}")
                            if (!details?.pincode.isNullOrBlank()) append(" - ${details?.pincode}")
                        }
                        DetailCard(
                            label       = "Address",
                            value       = addressFull.ifBlank { "—" },
                            icon        = Icons.Filled.LocationOn,
                            iconBgColor = Color(0xFFE3F2FD),
                            iconTint    = Color(0xFF1976D2),
                            editable    = true,
                            onEditClick = {
                                val intent = Intent(ctx, EditAddressActivity::class.java).apply {
                                    putExtra("contact_name", details?.contact_name.orEmpty())
                                    putExtra("company_name", details?.company_name.orEmpty())
                                    putExtra("address",      details?.address.orEmpty())
                                    putExtra("landmark",     details?.landmark.orEmpty())
                                    putExtra("city",         details?.city.orEmpty())
                                    putExtra("pincode",      details?.pincode.orEmpty())
                                }
                                ctx.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }

    // Edit dialogs
    when (editField) {
        "name" -> EditFieldDialog(
            title        = "Edit Name",
            currentValue = state.details?.contact_name.orEmpty(),
            onDismiss    = { editField = null },
            onSave       = { newName ->
                vm.editName(companyId, newName)
                editField = null
            }
        )
    }
}

@Composable
private fun DetailCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    editable: Boolean,
    onEditClick: () -> Unit = {}
) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .then(if (editable) Modifier.clickable { onEditClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored icon background
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = Color(0xFF999999), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(3.dp))
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
            }

            if (editable) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint     = Color(0xFF666666),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditFieldDialog(
    title: String,
    currentValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    saveEnabled: Boolean = true,
    hint: String? = null
) {
    var text by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(20.dp),
        title = {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1A1A1A))
        },
        text = {
            Column {
                OutlinedTextField(
                    value         = text,
                    onValueChange = { text = it },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = YD_RED,
                        unfocusedBorderColor = Color(0xFFDDDDDD)
                    )
                )
                if (hint != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(hint, fontSize = 11.sp, color = Color(0xFFAAAAAA))
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onSave(text) },
                enabled  = saveEnabled && text.isNotBlank(),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = YD_RED),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF888888))
            }
        }
    )
}
