package com.example.servicehub.ui.food

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.cart.CartManager
import com.example.servicehub.data.model.ItemDetail
import com.example.servicehub.data.repository.CartRepository
import com.example.servicehub.session.UserSession
import com.example.servicehub.ui.cart.CartBar
import com.example.servicehub.ui.cart.CartDetailsActivity
import com.example.servicehub.viewmodel.ItemDetailPageViewModel
import kotlinx.coroutines.launch

private const val EXTRA_ITEM_ID = "item_id"
private const val EXTRA_ITEM_TITLE = "item_title"
private const val BASE_IMG = "https://jmsn.in//images//appimage//"

private fun imgUrl(src: String?): String? {
    if (src.isNullOrBlank()) return null
    if (src.startsWith("http", true)) return src
    return BASE_IMG + src.trim().removePrefix("/")
}

class ItemDetailPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemId    = intent.getStringExtra(EXTRA_ITEM_ID).orEmpty()
        val itemTitle = intent.getStringExtra(EXTRA_ITEM_TITLE).orEmpty()
        setContent {
            ItemDetailPageScreen(
                itemId = itemId,
                itemTitle = itemTitle,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
private fun ItemDetailPageScreen(
    itemId: String,
    itemTitle: String,
    onBack: () -> Unit,
    vm: ItemDetailPageViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val cartRepo = remember { CartRepository() }

    LaunchedEffect(itemId) {
        if (itemId.isNotBlank()) vm.load(itemId)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            val context = androidx.compose.ui.platform.LocalContext.current
            CartBar(onCartClick = {
                context.startActivity(
                    android.content.Intent(context, CartDetailsActivity::class.java)
                )
            })
        }
    ) { padding ->
        when {
            state.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${state.error}", color = Color.Red)
                }
            }

            state.item != null -> {
                ItemDetailContent(
                    item = state.item!!,
                    modifier = Modifier.padding(padding),
                    onAddToCart = { item ->
                        CartManager.addOne(
                            item.itemId.orEmpty(),
                            item.itemName.orEmpty(),
                            item.itemPrice.orEmpty()
                        )
                        val newQty = CartManager.getQuantity(item.itemId.orEmpty())
                        scope.launch {
                            runCatching {
                                cartRepo.addToCart(
                                    UserSession.companyId,
                                    item.itemId.orEmpty(),
                                    newQty,
                                    item.itemPrice.orEmpty()
                                )
                            }
                        }
                    },
                    onRemoveFromCart = { item ->
                        CartManager.removeOne(item.itemId.orEmpty())
                        val newQty = CartManager.getQuantity(item.itemId.orEmpty())
                        scope.launch {
                            runCatching {
                                if (newQty == 0) {
                                    cartRepo.removeFromCart(
                                        UserSession.companyId,
                                        item.itemId.orEmpty(),
                                        1,
                                        item.itemPrice.orEmpty()
                                    )
                                } else {
                                    cartRepo.addToCart(
                                        UserSession.companyId,
                                        item.itemId.orEmpty(),
                                        newQty,
                                        item.itemPrice.orEmpty()
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDetailContent(
    item: ItemDetail,
    modifier: Modifier = Modifier,
    onAddToCart: (ItemDetail) -> Unit = {},
    onRemoveFromCart: (ItemDetail) -> Unit = {}
) {
    val entries by CartManager.entries.collectAsStateWithLifecycle()
    val qty = entries[item.itemId]?.quantity ?: 0
    var showNutritionSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val isOutOfStock = item.stock == "0"

    val pricePerKg = remember(item.itemPrice, item.itemCategory) {
        val price = item.itemPrice?.toDoubleOrNull()
        val weight = item.itemCategory?.toDoubleOrNull()
        if (price != null && weight != null && weight > 0)
            String.format("%.2f", price / weight)
        else null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Product image
        AsyncImage(
            model = imgUrl(item.imgsrc),
            contentDescription = item.itemName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFFF8F8F8))
        )

        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            // Product name
            Text(
                text = item.itemName.orEmpty(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(12.dp))

            // Weight chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1A3A6B),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = "${item.itemCategory.orEmpty()} kg",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Price card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A6B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Weight header
                    Text(
                        text = "${item.itemCategory.orEmpty()} kg",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "₹${item.itemPrice.orEmpty()}/bag",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                            if (pricePerKg != null) {
                                Text(
                                    text = "₹$pricePerKg/kg",
                                    color = Color(0xFFCCCCCC),
                                    fontSize = 13.sp
                                )
                            }
                            if (!item.brandName.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.brandName,
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // ADD / qty counter button
                        when {
                            isOutOfStock -> {
                                OutlinedButton(
                                    onClick = {},
                                    enabled = false,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF888888)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        disabledContentColor = Color(0xFF888888)
                                    )
                                ) {
                                    Text("No Stock", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            qty == 0 -> {
                                OutlinedButton(
                                    onClick = { onAddToCart(item) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text("ADD +", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            else -> {
                                Row(
                                    modifier = Modifier
                                        .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { onRemoveFromCart(item) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Text("$qty", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    IconButton(onClick = { onAddToCart(item) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Filled.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Delivery info card
            if (!item.delInfo.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF1A3A6B),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = item.delInfo,
                            fontSize = 14.sp,
                            color = Color(0xFF333333),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Description section
            if (!item.description.isNullOrBlank()) {
                SectionTitle(title = "Description")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.description,
                    fontSize = 14.sp,
                    color = Color(0xFF444444),
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(20.dp))
            }

            // Nutrition button
            if (!item.nutrition.isNullOrBlank()) {
                OutlinedButton(
                    onClick = { showNutritionSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1A3A6B)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A3A6B))
                ) {
                    Text("Nutrition Facts", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                }
                Spacer(Modifier.height(24.dp))
            }

            // Nutrition bottom sheet
            if (showNutritionSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showNutritionSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    NutritionBottomSheetContent(nutritionText = item.nutrition.orEmpty())
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color(0xFF111111)
    )
    Spacer(Modifier.height(4.dp))
    Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
}

private fun parseNutrition(text: String): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    val pattern = Regex("([A-Za-z_][A-Za-z0-9_ ]*):\\s*([\\d.]+\\s*(?:g|mg|mcg|Kcal|%|months?)?)")
    pattern.findAll(text).forEach { match ->
        val key = match.groupValues[1].trim().replace("_", " ")
        val value = match.groupValues[2].trim()
        if (key.isNotBlank() && value.isNotBlank()) result.add(key to value)
    }
    return result
}

private val nutrientColors = mapOf(
    "Energy"        to Color(0xFFFF8F00),
    "Carbohydrate"  to Color(0xFF2E7D32),
    "Protein"       to Color(0xFF1565C0),
    "Fat"           to Color(0xFFC62828),
    "Fibre"         to Color(0xFF00838F),
    "Minerals"      to Color(0xFF6A1B9A),
    "Folic Acid"    to Color(0xFF00695C),
)

private fun nutrientColor(key: String): Color {
    return nutrientColors.entries.firstOrNull { key.contains(it.key, ignoreCase = true) }?.value
        ?: Color(0xFF546E7A)
}

@Composable
private fun NutritionBottomSheetContent(nutritionText: String) {
    val allItems = remember(nutritionText) { parseNutrition(nutritionText) }

    val servingItems = allItems.filter {
        it.first.contains("nutrition", ignoreCase = true) ||
        it.first.contains("serving", ignoreCase = true)
    }
    val nutrientItems = allItems.filter {
        !it.first.contains("nutrition", ignoreCase = true) &&
        !it.first.contains("serving", ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Sheet header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A3A6B))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                Text(
                    text = "Nutrition Facts",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (servingItems.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        servingItems.forEach { (key, value) ->
                            Text(
                                text = "$key: $value",
                                color = Color(0xFFBBCCE8),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Nutrient rows in scrollable list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            itemsIndexed(nutrientItems) { index, (key, value) ->
                val color = nutrientColor(key)
                val bgColor = if (index % 2 == 0) Color(0xFFF7F9FF) else Color.White

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(36.dp)
                            .background(color, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = key,
                        fontSize = 14.sp,
                        color = Color(0xFF444444),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = value,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }
                Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
            }
        }
    }
}
