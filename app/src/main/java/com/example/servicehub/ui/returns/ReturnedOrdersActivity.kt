package com.example.servicehub.ui.returns

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.servicehub.data.model.ReturnedOrderItem
import com.example.servicehub.session.UserSession
import com.example.servicehub.viewmodel.ReturnViewModel

private val BRAND_RED    = Color(0xFFCC0000)
private val STATUS_GREEN = Color(0xFF2E7D32)
private val PAGE_BG      = Color(0xFFF2F2F2)

class ReturnedOrdersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ReturnViewModel = viewModel()
            val companyId = UserSession.companyId
            LaunchedEffect(companyId) { vm.loadReturnedOrders(companyId) }
            ReturnedOrdersScreen(vm = vm, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnedOrdersScreen(vm: ReturnViewModel, onBack: () -> Unit) {
    val state by vm.returnedState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = PAGE_BG,
        topBar = {
            TopAppBar(
                title = { Text("Returned Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            state.items.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No Returned Orders", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(8.dp))
                    Text("Your returned orders will appear here.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.items) { item ->
                        ReturnedOrderCard(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReturnedOrderCard(item: ReturnedOrderItem) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Order number + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.sales_order_number.orEmpty(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = STATUS_GREEN.copy(alpha = 0.12f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = STATUS_GREEN, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Returned", color = STATUS_GREEN, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Return Date: ${item.return_date.orEmpty()}", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Item info + amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon placeholder (no image in this API)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Replay, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(28.dp))
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(item.item_name.orEmpty(), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("Qty: ${item.quantity.orEmpty()}", fontSize = 12.sp, color = Color.Gray)
                    if (!item.reason.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text("Reason: ${item.reason}", fontSize = 12.sp, color = Color(0xFF888888))
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Amount", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "₹${item.amount.orEmpty()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = STATUS_GREEN
                    )
                }
            }
        }
    }
}
