package com.example.servicehub.ui.expiry

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.DateRange
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
import com.example.servicehub.data.model.ExpiryItem
import com.example.servicehub.session.UserSession
import com.example.servicehub.viewmodel.ExpiryViewModel
import java.util.Calendar

private val BRAND_RED = Color(0xFFCC0000)
private val PAGE_BG   = Color(0xFFF2F2F2)
private val IMG_BASE  = "https://jmsn.in//images//appimage//"

class ExpiryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ExpiryViewModel = viewModel()
            val companyId = UserSession.companyId
            LaunchedEffect(companyId) { vm.load(companyId) }
            ExpiryScreen(vm = vm, companyId = companyId, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryScreen(vm: ExpiryViewModel, companyId: String, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var returnTarget by remember { mutableStateOf<ExpiryItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionResult) {
        state.actionResult?.let {
            snackbarHostState.showSnackbar("Expiry return submitted successfully")
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
                title = { Text("Expiry Returns", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        ctx.startActivity(Intent(ctx, ExpiryReturnedActivity::class.java))
                    }) {
                        Text("Expiry Returned", color = BRAND_RED, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                    Icon(Icons.Filled.Replay, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No Expiry Items", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(8.dp))
                    Text("Items eligible for expiry return will appear here.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.items) { item ->
                        ExpiryItemCard(item = item, onCreateReturn = { returnTarget = it })
                    }
                }
            }
        }
    }

    returnTarget?.let { item ->
        CreateExpiryReturnSheet(
            item = item,
            onDismiss = { returnTarget = null },
            onSubmit = { mfgDate, expDate, qty ->
                vm.submitExpiryReturn(
                    salesOrderId = item.sales_order_id.orEmpty(),
                    companyId    = companyId,
                    itemId       = item.item_id.orEmpty(),
                    mfgDate      = mfgDate,
                    expDate      = expDate,
                    quantity     = qty
                )
                returnTarget = null
            }
        )
    }
}

@Composable
fun ExpiryItemCard(item: ExpiryItem, onCreateReturn: (ExpiryItem) -> Unit) {
    val canReturn = item.return_status.equals("Create Return", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(14.dp),
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
                Text(item.sales_order_number.orEmpty(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF333333))
                Text(item.order_date.orEmpty(), fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(item.item_name.orEmpty(), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Qty: ${item.quantity.orEmpty()}", fontSize = 12.sp, color = Color.Gray)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Amount", fontSize = 11.sp, color = Color.Gray)
                    Text("₹${item.item_price.orEmpty()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
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
fun CreateExpiryReturnSheet(
    item: ExpiryItem,
    onDismiss: () -> Unit,
    onSubmit: (mfgDate: String, expDate: String, quantity: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val ctx = LocalContext.current

    var mfgDate by remember { mutableStateOf("") }
    var expDate by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    val maxQty = item.quantity?.toIntOrNull() ?: 1

    val qtyInt = quantityText.toIntOrNull() ?: 0
    val qtyValid = qtyInt in 1..maxQty
    val canSubmit = mfgDate.isNotBlank() && expDate.isNotBlank() && qtyValid

    fun showDatePicker(onDatePicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            ctx,
            { _, year, month, day ->
                onDatePicked("%04d-%02d-%02d".format(year, month + 1, day))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

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
            Text("Create Expiry Return", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(item.item_name.orEmpty(), fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            // Mfg Date + Exp Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = mfgDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mfg Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker { mfgDate = it } }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Pick mfg date", tint = BRAND_RED)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = expDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Exp Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker { expDate = it } }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Pick exp date", tint = BRAND_RED)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Quantity
            OutlinedTextField(
                value = quantityText,
                onValueChange = { v ->
                    if (v.all { it.isDigit() } && v.length <= 3) quantityText = v
                },
                label = { Text("Quantity") },
                placeholder = { Text("Max $maxQty") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                isError = quantityText.isNotBlank() && !qtyValid,
                modifier = Modifier.width(140.dp)
            )
            if (quantityText.isNotBlank() && !qtyValid) {
                Text("Max $maxQty", fontSize = 10.sp, color = Color.Red, modifier = Modifier.padding(start = 4.dp))
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { if (canSubmit) onSubmit(mfgDate, expDate, qtyInt) },
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
