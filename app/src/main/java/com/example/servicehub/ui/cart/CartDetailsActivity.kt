package com.example.servicehub.ui.cart

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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.data.model.CartDetails
import com.example.servicehub.data.model.CartItemFlat
import com.example.servicehub.session.UserSession
import com.example.servicehub.viewmodel.CartDetailsViewModel

private const val BASE_IMAGE_URL = "https://jmsn.in//images//appimage//"

private fun imgUrl(src: String?): String? {
    if (src.isNullOrBlank()) return null
    if (src.startsWith("http", true)) return src
    return BASE_IMAGE_URL + src.trim().removePrefix("/")
}

class CartDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CartDetailsScreen(onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartDetailsScreen(
    onBack: () -> Unit,
    vm: CartDetailsViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.load(UserSession.companyId)
    }

    Scaffold(
        containerColor = Color(0xFFF2F2F7),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Food and FMCG Cart",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Options",
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF1A1A1A), RoundedCornerShape(50))
                                .padding(4.dp),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            state.details?.let { details ->
                CartBottomCheckout(details = details)
            }
        }
    ) { padding ->
        when {
            state.loading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color(0xFFD32F2F)) }
            }

            state.error != null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            state.error!!,
                            color = Color(0xFF555555),
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            state.details != null -> {
                CartContent(
                    details = state.details!!,
                    modifier = Modifier.padding(padding),
                    vm = vm
                )
            }

            else -> {
                // API returned success but empty data — cart is empty
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛒", fontSize = 56.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Your cart is empty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Add items from the product listing to get started.",
                            fontSize = 14.sp,
                            color = Color(0xFF888888),
                            modifier = Modifier.padding(horizontal = 40.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartContent(
    details: CartDetails,
    modifier: Modifier = Modifier,
    vm: CartDetailsViewModel
) {
    val items = remember(details) { details.parsedItems() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        item { DeliverToCard(address = details.companyAddress.orEmpty()) }

        if (!details.delInfo.isNullOrBlank()) {
            item { DelayedDeliveryBanner(delInfo = details.delInfo) }
        }

        item { CartSectionHeader(count = items.size) }

        items(items) { item ->
            CartItemCard(
                item = item,
                onDelete = { itemId ->
                    vm.deleteOne(
                        itemId = itemId,
                        price = item.price
                    )
                }
            )
        }

        // Bill Summary — matches screenshot design
        item {
            BillSummaryCard(
                items = items,
                shippingCharge = details.shippingCharge.orEmpty(),
                shippingAmount = details.shipping.orEmpty()
            )
        }

        // Policy section
        if (!details.cancellation.isNullOrBlank() || !details.returns.isNullOrBlank()) {
            item {
                PolicyCard(
                    cancellation = details.cancellation.orEmpty(),
                    returns = details.returns.orEmpty(),
                    readPolicy = details.readPolicy.orEmpty()
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

// ── Deliver To ──────────────────────────────────────────────────────────────

@Composable
private fun DeliverToCard(address: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = Color(0xFF1A56C4),
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Deliver to ${UserSession.phone.ifBlank { "My Address" }}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = address,
                    fontSize = 12.sp,
                    color = Color(0xFF777777),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Change",
                color = Color(0xFF1A56C4),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

// ── Delayed Delivery Banner ──────────────────────────────────────────────────

@Composable
private fun DelayedDeliveryBanner(delInfo: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalShipping,
                    contentDescription = null,
                    tint = Color(0xFF1A56C4),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Delivery Info",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = delInfo,
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ── Bill Summary ─────────────────────────────────────────────────────────────

@Composable
private fun BillSummaryCard(
    items: List<CartItemFlat>,
    shippingCharge: String,
    shippingAmount: String
) {
    val isFreeShipping = shippingCharge.equals("Free", ignoreCase = true)
    val itemsTotal = items.sumOf {
        (it.price.toDoubleOrNull() ?: 0.0) * (it.quantity.toIntOrNull() ?: 1)
    }
    val shippingCost = if (isFreeShipping) 0.0 else shippingAmount.toDoubleOrNull() ?: 0.0
    val grandTotal = itemsTotal + shippingCost
    val itemCount = items.sumOf { it.quantity.toIntOrNull() ?: 1 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFBBBBBB))
            Text(
                text = "  BILL SUMMARY  ",
                fontSize = 12.sp,
                color = Color(0xFF888888),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFBBBBBB))
        }

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Price (N items)
                BillRow(
                    label = "Price ($itemCount item${if (itemCount > 1) "s" else ""})",
                    value = "₹${String.format("%,.2f", itemsTotal)}",
                    valueColor = Color(0xFF1A1A1A)
                )

                Divider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(vertical = 10.dp))

                // Item Total A
                BillRow(
                    label = "Item Total  A",
                    value = "₹${String.format("%,.2f", itemsTotal)}",
                    labelBold = true,
                    valueColor = Color(0xFF1A1A1A)
                )

                Divider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(vertical = 10.dp))

                // Shipping Charges B — with strikethrough if free
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shipping Charges  B",
                        fontSize = 14.sp,
                        color = Color(0xFF444444),
                        fontWeight = FontWeight.Bold
                    )
                    if (isFreeShipping && shippingAmount.isNotBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FREE",
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${shippingAmount}",
                                fontSize = 13.sp,
                                color = Color(0xFF999999),
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    } else {
                        Text(
                            text = "₹${String.format("%,.2f", shippingCost)}",
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Divider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(vertical = 10.dp))

                // Total Order Amount C = A + B
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A1A))) {
                                append("Total Order Amount  ")
                            }
                            withStyle(SpanStyle(fontSize = 12.sp, color = Color(0xFF888888))) {
                                append("C = A + B")
                            }
                        }
                    )
                    Text(
                        text = "₹${String.format("%,.2f", grandTotal)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
        }
    }
}

// ── Items in Cart Section Header ─────────────────────────────────────────────

@Composable
private fun CartSectionHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f), color = Color(0xFFBBBBBB))
        Text(
            text = "  ITEMS IN CART ($count)  ",
            fontSize = 12.sp,
            color = Color(0xFF888888),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Divider(modifier = Modifier.weight(1f), color = Color(0xFFBBBBBB))
    }
}

// ── Cart Item Card ───────────────────────────────────────────────────────────

@Composable
private fun CartItemCard(
    item: CartItemFlat,
    onDelete: (itemId: String) -> Unit
) {
    val qty = item.quantity.toIntOrNull() ?: 1
    val unitPrice = item.price.toDoubleOrNull() ?: 0.0
    val total = unitPrice * qty

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Product image
                AsyncImage(
                    model = imgUrl(item.imgsrc),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF5F5F5))
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.height(4.dp))
                    if (item.category.isNotBlank()) {
                        Text(
                            text = item.category,
                            fontSize = 13.sp,
                            color = Color(0xFF666666)
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        text = "₹${item.price} / pc",
                        fontSize = 13.sp,
                        color = Color(0xFF444444)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFFF5F5F5))
            Spacer(Modifier.height(10.dp))

            // Bottom row: [🗑 trash] [N Pc ▼] [₹total]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onDelete(item.itemId) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFFF3F3), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete item",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                }

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCCCCCC)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A1A1A)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$qty Pc",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "₹${String.format("%,.2f", total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A1A)
                )
            }
        }
    }
}

// ── Bill Row helper ───────────────────────────────────────────────────────────

@Composable
private fun BillRow(
    label: String,
    value: String,
    labelBold: Boolean = false,
    valueColor: Color = Color(0xFF1A1A1A)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF444444),
            fontWeight = if (labelBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (labelBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

// ── Policy Card ──────────────────────────────────────────────────────────────

@Composable
private fun PolicyCard(cancellation: String, returns: String, readPolicy: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFBBBBBB))
            Text(
                text = "  POLICY  ",
                fontSize = 12.sp,
                color = Color(0xFF888888),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFBBBBBB))
        }

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Cancellation
                if (cancellation.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFFF3F3), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cancellation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = cancellation,
                                fontSize = 13.sp,
                                color = Color(0xFF555555),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                if (cancellation.isNotBlank() && returns.isNotBlank()) {
                    Divider(
                        color = Color(0xFFF0F0F0),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                // Returns
                if (returns.isNotBlank()) {
                    val context = LocalContext.current
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE8F0FE), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Replay,
                                contentDescription = null,
                                tint = Color(0xFF1A56C4),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Returns",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = returns,
                                fontSize = 13.sp,
                                color = Color(0xFF555555),
                                lineHeight = 18.sp
                            )
                            if (readPolicy.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = readPolicy,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1A56C4),
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable {
                                        context.startActivity(
                                            android.content.Intent(
                                                context,
                                                com.example.servicehub.ui.policy.PolicyActivity::class.java
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Bottom Checkout Bar ───────────────────────────────────────────────────────

@Composable
private fun CartBottomCheckout(details: CartDetails) {
    val items = remember(details) { details.parsedItems() }
    val itemsTotal = items.sumOf {
        (it.price.toDoubleOrNull() ?: 0.0) * (it.quantity.toIntOrNull() ?: 1)
    }
    val isFreeShipping = details.shippingCharge.equals("Free", ignoreCase = true)
    val shipping = if (isFreeShipping) 0.0 else details.shipping?.toDoubleOrNull() ?: 0.0
    val grandTotal = itemsTotal + shipping

    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Free delivery progress hint
            if (!isFreeShipping) {
                val freeThreshold = 1119.0
                val remaining = (freeThreshold - itemsTotal).coerceAtLeast(0.0)
                if (remaining > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Add ₹${String.format("%.0f", remaining)} more for FREE Delivery",
                                fontSize = 13.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "₹ ${String.format("%,.2f", grandTotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "VIEW BILL DETAILS",
                        fontSize = 11.sp,
                        color = Color(0xFF1A56C4),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier
                        .height(50.dp)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = "Proceed to Buy",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
