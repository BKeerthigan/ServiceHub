package com.example.servicehub.ui.delivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.data.model.CancelDetailData
import com.example.servicehub.data.model.CancelDetailItem
import com.example.servicehub.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private val PAGE_BG  = Color(0xFFF2F2F2)
private val IMG_BASE = "https://jmsn.in//images//appimage//"

// ── Status helpers ────────────────────────────────────────────────────────────

private data class StatusStyle(
    val label: String,
    val color: Color,
    val icon: ImageVector
)

private fun statusStyle(delMessage: String?, status: String?): StatusStyle {
    val msg = delMessage?.lowercase().orEmpty()
    return when {
        msg == "cancel"            -> StatusStyle("Order Cancelled By You", Color(0xFFCC0000), Icons.Filled.Cancel)
        msg == "delivered"         -> StatusStyle("Order Delivered",        Color(0xFF2E7D32), Icons.Filled.CheckCircle)
        msg == "sent to delivery"  -> StatusStyle("Out for Delivery",       Color(0xFF1565C0), Icons.Filled.LocalShipping)
        status?.lowercase() == "new" || msg == "pending"
                                   -> StatusStyle("Order Placed",           Color(0xFFF57C00), Icons.Filled.HourglassTop)
        else                       -> StatusStyle(
            (status ?: delMessage).orEmpty().replaceFirstChar { it.uppercase() },
            Color(0xFF555555), Icons.Filled.Info
        )
    }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

data class DeliveryDetailUiState(
    val detail: CancelDetailData? = null,
    val loading: Boolean = true,
    val error: String? = null
)

class DeliveryDetailViewModel(private val salesOrderId: String) : ViewModel() {
    private val repo = DeliveryRepository()
    private val _state = MutableStateFlow(DeliveryDetailUiState())
    val state: StateFlow<DeliveryDetailUiState> = _state

    init {
        viewModelScope.launch {
            try {
                val data = repo.getDeliveryDetail(salesOrderId)
                _state.value = DeliveryDetailUiState(detail = data, loading = false)
            } catch (e: Exception) {
                _state.value = DeliveryDetailUiState(loading = false, error = e.message)
            }
        }
    }
}

class DeliveryDetailVmFactory(private val salesOrderId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DeliveryDetailViewModel(salesOrderId) as T
    }
}

// ── Activity ──────────────────────────────────────────────────────────────────

class DeliveryDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val salesOrderId = intent.getStringExtra("sales_order_id") ?: ""
        setContent {
            val vm: DeliveryDetailViewModel = viewModel(factory = DeliveryDetailVmFactory(salesOrderId))
            DeliveryDetailScreen(vm = vm, onBack = { finish() })
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailScreen(vm: DeliveryDetailViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = PAGE_BG,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Delivery Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        state.detail?.order_date?.let {
                            Text("Ordered on $it", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {}) {
                        Text("Support", color = Color(0xFFCC0000), fontWeight = FontWeight.Medium)
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
                    CircularProgressIndicator(color = Color(0xFFCC0000))
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.error}", color = Color.Red)
                }
            }
            state.detail != null -> {
                val detail = state.detail!!
                val items = detail.parsedItems()
                val style = statusStyle(detail.del_message, detail.status)
                val itemsTotal = items.sumOf {
                    (it.price.toDoubleOrNull() ?: 0.0) * (it.quantity.toIntOrNull() ?: 1)
                }
                val shippingFree = detail.shipping_charge.equals("Free", ignoreCase = true)
                val shippingAmt  = if (shippingFree) 0.0 else detail.shipping?.toDoubleOrNull() ?: 0.0
                val grandTotal   = detail.net_value?.toDoubleOrNull() ?: (itemsTotal + shippingAmt)

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Status card
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        style.label,
                                        color = style.color,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(detail.order_date.orEmpty(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("Total product count", fontSize = 12.sp, color = Color.Gray)
                                Text("${items.size}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("Order Amount", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    "₹ ${String.format("%,.0f", grandTotal)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    // Products header
                    item {
                        Text(
                            "${items.size} Product${if (items.size != 1) "s" else ""} in this Delivery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Product cards
                    items(items.size) { idx ->
                        DeliveryProductRow(items[idx])
                    }

                    // Bill Summary
                    item {
                        DeliveryBillSummary(items, itemsTotal, shippingFree, detail.shipping.orEmpty())
                    }

                    // Address
                    if (!detail.address.isNullOrBlank()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFFCC0000),
                                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(detail.address, fontSize = 13.sp, lineHeight = 20.sp)
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DeliveryProductRow(item: CancelDetailItem) {
    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.imgSrc.isNotBlank()) {
                AsyncImage(
                    model = IMG_BASE + item.imgSrc,
                    contentDescription = item.itemName,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.itemName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                val qty = item.quantity.toIntOrNull() ?: 1
                Text("$qty Pc${if (qty > 1) "s" else ""}", fontSize = 13.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Amount", fontSize = 11.sp, color = Color.Gray)
                val total = (item.price.toDoubleOrNull() ?: 0.0) * (item.quantity.toIntOrNull() ?: 1)
                Text("₹${String.format("%,.2f", total)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun DeliveryBillSummary(
    items: List<CancelDetailItem>,
    itemsTotal: Double,
    shippingFree: Boolean,
    shipping: String
) {
    val shippingNum = shipping.toDoubleOrNull() ?: 0.0
    val totalAmount = if (shippingFree) itemsTotal else itemsTotal + shippingNum

    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("  BILL SUMMARY  ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            BillRow("Price (${items.size} item${if (items.size != 1) "s" else ""})", "₹${String.format("%,.2f", itemsTotal)}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            BillRow("Item Total  A", "₹${String.format("%,.2f", itemsTotal)}", bold = true)
            Spacer(Modifier.height(8.dp))
            // Shipping charges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Shipping Charges  B", fontSize = 13.sp)
                if (shippingFree) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("FREE", fontSize = 13.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                        if (shippingNum > 0) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "₹${String.format("%,.0f", shippingNum)}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                } else {
                    Text("₹${String.format("%,.2f", shippingNum)}", fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            BillRow("Total Order Amount  C = A + B", "₹${String.format("%,.2f", totalAmount)}", bold = true)
        }
    }
}

@Composable
private fun BillRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal)
        Text(value, fontSize = 13.sp, fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal)
    }
}
