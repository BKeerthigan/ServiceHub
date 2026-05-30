package com.example.servicehub.ui.delivery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.servicehub.data.model.DeliveryItem
import com.example.servicehub.session.UserSession
import com.example.servicehub.viewmodel.DeliveryViewModel

private val BRAND_RED     = Color(0xFFCC0000)
private val STATUS_ORANGE = Color(0xFFF57C00)
private val STATUS_GREEN  = Color(0xFF2E7D32)
private val STATUS_BLUE   = Color(0xFF1565C0)
private val PAGE_BG       = Color(0xFFF2F2F2)

class DeliveryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: DeliveryViewModel = viewModel()
            val companyId = UserSession.companyId
            Log.d("DELIVERY", "UserSession.companyId = '$companyId'")
            LaunchedEffect(companyId) { vm.load(companyId) }
            DeliveryScreen(vm = vm, companyId = companyId, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen(vm: DeliveryViewModel, companyId: String, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var cancelTarget by remember { mutableStateOf<DeliveryItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionResult) {
        state.actionResult?.let {
            snackbarHostState.showSnackbar("Order $it successfully")
            vm.clearActionResult()
        }
    }
    LaunchedEffect(state.actionError) {
        state.actionError?.let {
            snackbarHostState.showSnackbar("Failed: $it")
            vm.clearActionResult()
        }
    }

    Scaffold(
        containerColor = PAGE_BG,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Deliveries", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        ctx.startActivity(Intent(ctx, CancelledDeliveryActivity::class.java))
                    }) {
                        Text(
                            "Cancelled Deliveries",
                            color = BRAND_RED,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        when {
            state.loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BRAND_RED)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.error}", color = Color.Red)
                }
            }
            state.activeItems.isEmpty() -> {
                EmptyDeliveryState(Modifier.fillMaxSize().padding(padding))
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.activeItems) { order ->
                        DeliveryCard(
                            order = order,
                            onCancel = { cancelTarget = it },
                            onReorder = { vm.submitAction(it.salesOrderId, "Reorder", companyId) }
                        )
                    }
                }
            }
        }
    }

    cancelTarget?.let { order ->
        CancelBottomSheet(
            order = order,
            onDismiss = { cancelTarget = null },
            onConfirm = { reason ->
                vm.submitAction(order.salesOrderId, reason, companyId)
                cancelTarget = null
            }
        )
    }
}

@Composable
private fun EmptyDeliveryState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.LocalShipping,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No Deliveries Found",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.DarkGray
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "After placing your order, it will appear here as a delivery.\nYou can track and see details of your delivery from here.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun DeliveryCard(
    order: DeliveryItem,
    onCancel: (DeliveryItem) -> Unit,
    onReorder: (DeliveryItem) -> Unit
) {
    val statusColor = when (order.delMessage.lowercase()) {
        "pending"          -> STATUS_ORANGE
        "sent to delivery" -> STATUS_BLUE
        "delivered"        -> STATUS_GREEN
        else               -> Color.Gray
    }
    val statusLabel = when (order.delMessage.lowercase()) {
        "pending"          -> "Pending"
        "sent to delivery" -> "On the Way"
        "delivered"        -> "Delivered"
        else               -> order.delMessage
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(order.salesOrderNumber, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Date", fontSize = 11.sp, color = Color.Gray)
                    Text(order.orderDate, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Order Amount", fontSize = 11.sp, color = Color.Gray)
                    Text("₹${order.netValue}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                when (order.delMessage.lowercase()) {
                    "pending" -> Button(
                        onClick = { onCancel(order) },
                        colors = ButtonDefaults.buttonColors(containerColor = BRAND_RED),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) { Text("Cancel", fontSize = 13.sp) }

                    "sent to delivery" -> Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFFDDDDDD),
                            disabledContentColor = Color(0xFF888888)
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) { Text("On the Way", fontSize = 13.sp) }

                    "delivered" -> Button(
                        onClick = { onReorder(order) },
                        colors = ButtonDefaults.buttonColors(containerColor = STATUS_GREEN),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) { Text("Reorder", fontSize = 13.sp) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelBottomSheet(
    order: DeliveryItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedReason by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Cancel the entire delivery?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text("Select reason for cancellation", fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(20.dp))

            order.cancelReasons.forEach { reason ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedReason == reason,
                        onCheckedChange = { selectedReason = if (it) reason else "" },
                        colors = CheckboxDefaults.colors(checkedColor = BRAND_RED)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(reason, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Not now") }

                Button(
                    onClick = { if (selectedReason.isNotBlank()) onConfirm(selectedReason) },
                    enabled = selectedReason.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BRAND_RED)
                ) { Text("Yes, Cancel") }
            }
        }
    }
}
