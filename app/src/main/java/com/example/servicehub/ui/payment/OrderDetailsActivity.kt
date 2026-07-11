package com.example.servicehub.ui.payment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.data.model.OrderDetailsData
import com.example.servicehub.data.model.OrderDetailsItemApi
import com.example.servicehub.ui.home.HomeActivity
import com.example.servicehub.viewmodel.OrderDetailsViewModel

private val GREEN   = Color(0xFF2E7D32)
private val PAGE_BG = Color(0xFFF2F2F2)
private const val IMG_BASE = "https://jmsn.in//images//appimage//"

class OrderDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val salesOrderId = intent.getStringExtra("sales_order_id") ?: ""
        setContent {
            val vm: OrderDetailsViewModel = viewModel()
            LaunchedEffect(salesOrderId) { vm.load(salesOrderId) }
            OrderDetailsScreen(vm = vm, onBack = {
                startActivity(
                    Intent(this, HomeActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
                finish()
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailsScreen(vm: OrderDetailsViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx   = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GREEN,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = PAGE_BG
    ) { padding ->
        when {
            state.loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GREEN)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = Color.Red, modifier = Modifier.padding(16.dp))
                }
            }
            state.data != null -> {
                val d = state.data!!
                val items = d.items.filter { !it.itemId.isNullOrBlank() }
                val itemsTotal = items.sumOf {
                    (it.price?.toDoubleOrNull() ?: 0.0) * (it.quantity?.toIntOrNull() ?: 1)
                }
                val shippingFree = d.shippingCharge.equals("Free", ignoreCase = true)
                val shippingAmt  = if (shippingFree) 0.0 else d.shipping?.toDoubleOrNull() ?: 0.0
                val grandTotal   = d.netValue?.toDoubleOrNull() ?: (itemsTotal + shippingAmt)

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Order header
                    item {
                        DetailCard {
                            SectionTitle("Order Information")
                            DetailRow("Ordered on", d.orderedOn.orEmpty())
                            DetailRow("Order ID",   d.orderId.orEmpty())
                            DetailRow("Items", "${items.size} item${if (items.size != 1) "s" else ""}")
                        }
                    }

                    // Product cards
                    if (items.isNotEmpty()) {
                        items(items.size) { idx ->
                            ProductRow(items[idx])
                        }
                    }

                    // Bill details
                    item {
                        DetailCard {
                            SectionTitle("Bill Details")
                            val qty = items.size
                            BillRow(
                                "Price ($qty item${if (qty != 1) "s" else ""})",
                                "₹ ${String.format("%,.2f", itemsTotal)}"
                            )
                            if (!shippingFree && shippingAmt > 0) {
                                BillRow("Delivery", "₹ ${String.format("%,.2f", shippingAmt)}")
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            BillRow(
                                "Total Amount",
                                "₹ ${String.format("%,.0f", grandTotal)}",
                                bold = true
                            )
                        }
                    }

                    // Payment info
                    if (!d.payInfo.isNullOrBlank() || !d.payNotes1.isNullOrBlank() || !d.payNotes2.isNullOrBlank()) {
                        item {
                            DetailCard {
                                SectionTitle("Payment Information")
                                if (!d.payInfo.isNullOrBlank())   DetailRow("Method", d.payInfo)
                                if (!d.payNotes1.isNullOrBlank()) DetailRow("", d.payNotes1)
                                if (!d.payNotes2.isNullOrBlank()) DetailRow("", d.payNotes2)
                            }
                        }
                    }

                    // Delivery address
                    if (!d.address.isNullOrBlank()) {
                        item {
                            Card(
                                modifier  = Modifier.fillMaxWidth(),
                                shape     = RoundedCornerShape(14.dp),
                                colors    = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = GREEN,
                                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Delivery Address",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF777777)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(d.address, fontSize = 14.sp, color = Color(0xFF444444), lineHeight = 20.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Delivery status
                    if (!d.deliveryDate.isNullOrBlank() || !d.deliveryStatus.isNullOrBlank()) {
                        item {
                            DetailCard {
                                SectionTitle("Delivery Status")
                                if (!d.deliveryDate.isNullOrBlank())   DetailRow("Expected by", d.deliveryDate)
                                if (!d.deliveryStatus.isNullOrBlank()) DetailRow("Status", d.deliveryStatus.replaceFirstChar { it.uppercase() })
                            }
                        }
                    }

                    // Call Us
                    if (!d.callUs.isNullOrBlank()) {
                        item {
                            Button(
                                onClick = {
                                    ctx.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${d.callUs}"))
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GREEN)
                            ) {
                                Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Call Us  ${d.callUs}", fontWeight = FontWeight.Bold, color = Color.White)
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
private fun ProductRow(item: OrderDetailsItemApi) {
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
            if (!item.imgsrc.isNullOrBlank()) {
                AsyncImage(
                    model = IMG_BASE + item.imgsrc,
                    contentDescription = item.itemName,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.itemName.orEmpty(), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                val qty = item.quantity?.toIntOrNull() ?: 1
                Text("${qty} Pc${if (qty > 1) "s" else ""}", fontSize = 13.sp, color = Color.Gray)
                if (!item.itemCategory.isNullOrBlank()) {
                    Text("${item.itemCategory} kg", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Amount", fontSize = 11.sp, color = Color.Gray)
                val total = (item.price?.toDoubleOrNull() ?: 0.0) * (item.quantity?.toIntOrNull() ?: 1)
                Text("₹${String.format("%,.2f", total)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text          = title,
        fontWeight    = FontWeight.Bold,
        fontSize      = 13.sp,
        color         = Color(0xFF777777),
        letterSpacing = 0.4.sp
    )
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun DetailRow(
    key: String,
    value: String,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (key.isNotBlank()) {
            Text(
                key,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            value,
            fontSize = 14.sp,
            color = Color(0xFF1A1A1A),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            modifier = if (key.isBlank()) Modifier else Modifier.weight(1f),
            textAlign = if (key.isBlank()) androidx.compose.ui.text.style.TextAlign.Start
                        else androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun BillRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF666666), fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontSize = 14.sp, color = Color(0xFF1A1A1A), fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold)
    }
}
