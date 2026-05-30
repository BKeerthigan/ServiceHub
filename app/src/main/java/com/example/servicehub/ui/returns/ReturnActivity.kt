package com.example.servicehub.ui.returns

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.data.model.ReturnItem
import com.example.servicehub.session.UserSession
import com.example.servicehub.viewmodel.ReturnViewModel

private val BRAND_RED = Color(0xFFCC0000)
private val PAGE_BG   = Color(0xFFF2F2F2)
private val IMG_BASE  = "https://jmsn.in//images//appimage//"

class ReturnActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ReturnViewModel = viewModel()
            val companyId = UserSession.companyId
            LaunchedEffect(companyId) { vm.load(companyId) }
            ReturnScreen(vm = vm, companyId = companyId, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnScreen(vm: ReturnViewModel, companyId: String, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var returnTarget by remember { mutableStateOf<ReturnItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionResult) {
        state.actionResult?.let {
            snackbarHostState.showSnackbar("Return submitted successfully")
            vm.clearResult()
        }
    }
    LaunchedEffect(state.actionError) {
        state.actionError?.let {
            snackbarHostState.showSnackbar("Failed: $it")
            vm.clearResult()
        }
    }

    Scaffold(
        containerColor = PAGE_BG,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Returns", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        ctx.startActivity(Intent(ctx, ReturnedOrdersActivity::class.java))
                    }) {
                        Text("Returned Orders", color = BRAND_RED, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Replay, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No Returns Available", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(8.dp))
                    Text("Delivered orders eligible for return will appear here.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.activeItems) { item ->
                        ReturnItemCard(item = item, onCreateReturn = { returnTarget = it })
                    }
                }
            }
        }
    }

    returnTarget?.let { item ->
        CreateReturnSheet(
            item = item,
            onDismiss = { returnTarget = null },
            onSubmit = { reason, qty ->
                vm.submitReturn(
                    salesOrderId = item.sales_order_id.orEmpty(),
                    companyId    = companyId,
                    itemId       = item.item_id.orEmpty(),
                    reason       = reason,
                    quantity     = qty
                )
                returnTarget = null
            }
        )
    }
}

@Composable
fun ReturnItemCard(item: ReturnItem, onCreateReturn: (ReturnItem) -> Unit) {
    val canReturn = item.return_status.equals("Create Return", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Order number + date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.sales_order_number.orEmpty(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF333333)
                )
                Text(item.order_date.orEmpty(), fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(12.dp))

            // Image + item info + amount
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Fixed-size image box
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = IMG_BASE + item.imgsrc.orEmpty(),
                        contentDescription = item.item_name,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                // Name + qty
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.item_name.orEmpty(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Qty: ${item.quantity.orEmpty()}", fontSize = 12.sp, color = Color.Gray)
                }

                // Price
                Column(horizontalAlignment = Alignment.End) {
                    Text("Amount", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "₹${item.item_price.orEmpty()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = { if (canReturn) onCreateReturn(item) },
                    enabled = canReturn,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BRAND_RED,
                        disabledContainerColor = Color(0xFFDDDDDD),
                        disabledContentColor = Color(0xFF888888)
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (canReturn) "Create Return" else item.return_status.orEmpty(),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReturnSheet(
    item: ReturnItem,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, quantity: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var reason by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    val maxQty = item.quantity?.toIntOrNull() ?: 1

    val qtyInt = quantityText.toIntOrNull() ?: 0
    val qtyValid = qtyInt in 1..maxQty
    val canSubmit = reason.isNotBlank() && qtyValid

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Create Return", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(item.item_name.orEmpty(), fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Reason text area
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    placeholder = { Text("e.g. Damaged Product") },
                    modifier = Modifier.weight(1f).height(120.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp)
                )

                // Quantity input
                Column(modifier = Modifier.width(90.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { v ->
                            if (v.all { it.isDigit() } && v.length <= 3) quantityText = v
                        },
                        label = { Text("Qty") },
                        placeholder = { Text("Max $maxQty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        isError = quantityText.isNotBlank() && !qtyValid
                    )
                    if (quantityText.isNotBlank() && !qtyValid) {
                        Text("Max $maxQty", fontSize = 10.sp, color = Color.Red, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { if (canSubmit) onSubmit(reason, qtyInt) },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BRAND_RED)
            ) {
                Text("Submit", fontSize = 15.sp)
            }
        }
    }
}
