package com.example.servicehub.ui.food

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.cart.CartManager
import com.example.servicehub.data.model.ItemDetail
import com.example.servicehub.data.model.ProductItem
import com.example.servicehub.data.repository.CartRepository
import com.example.servicehub.session.UserSession
import com.example.servicehub.ui.cart.CartBar
import com.example.servicehub.ui.cart.CartDetailsActivity
import com.example.servicehub.viewmodel.ProductDetailsViewModel
import kotlinx.coroutines.launch

private const val EXTRA_LIST_ID = "listid"
private const val EXTRA_PIID    = "piid"
private const val EXTRA_TITLE   = "title"
private const val BASE_IMAGE_URL = "https://jmsn.in//images//appimage//"

private fun fullImageUrl(img: String?): String? {
    if (img.isNullOrBlank()) return null
    if (img.startsWith("http", true)) return img
    return BASE_IMAGE_URL + img.trim().removePrefix("/")
}

class ProductDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listId = intent.getStringExtra(EXTRA_LIST_ID).orEmpty()
        val piid   = intent.getStringExtra(EXTRA_PIID).orEmpty()
        val title  = intent.getStringExtra(EXTRA_TITLE).orEmpty()

        setContent {
            ProductDetailsScreen(
                listId = listId,
                initialPiid = piid,
                initialTitle = title,
                onBack = { finish() },
                onItemClick = { itemId, itemTitle ->
                    val intent = android.content.Intent(this, ItemDetailPageActivity::class.java).apply {
                        putExtra("item_id", itemId)
                        putExtra("item_title", itemTitle)
                    }
                    startActivity(intent)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailsScreen(
    listId: String,
    initialPiid: String,
    initialTitle: String,
    onBack: () -> Unit,
    onItemClick: (itemId: String, itemTitle: String) -> Unit,
    vm: ProductDetailsViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var currentTitle by remember { mutableStateOf(initialTitle) }

    val scope = rememberCoroutineScope()
    val cartRepo = remember { CartRepository() }

    LaunchedEffect(listId, initialPiid) {
        vm.init(listId, initialPiid)
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentTitle.ifBlank { "Products" },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LeftPanel(
                siblings = state.siblings,
                selectedPiid = state.selectedPiid,
                onItemClick = { item ->
                    currentTitle = item.name.orEmpty()
                    vm.loadForPiid(item.piid.orEmpty())
                }
            )

            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = Color(0xFFE0E0E0)
            )

            RightPanel(
                title = currentTitle,
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                items = vm.filteredItems(),
                loading = state.loading,
                error = state.error,
                onCategoryClick = { cat ->
                    vm.selectCategory(if (state.selectedCategory == cat) null else cat)
                },
                onItemClick = onItemClick,
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

@Composable
private fun LeftPanel(
    siblings: List<ProductItem>,
    selectedPiid: String,
    onItemClick: (ProductItem) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .width(110.dp)
            .fillMaxHeight()
            .background(Color.White)
    ) {
        items(siblings) { item ->
            val isSelected = item.piid == selectedPiid
            SiblingItem(
                item = item,
                isSelected = isSelected,
                onClick = { onItemClick(item) }
            )
            Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun SiblingItem(
    item: ProductItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor   = if (isSelected) Color(0xFFE8F0FE) else Color.White
    val textColor = if (isSelected) Color(0xFF1A56C4) else Color(0xFF333333)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .then(
                if (isSelected)
                    Modifier.border(
                        width = 3.dp,
                        color = Color(0xFF1A56C4),
                        shape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp)
                    )
                else Modifier
            )
            .clickable { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = fullImageUrl(item.imgsrc),
                contentDescription = item.name,
                modifier = Modifier
                    .size(70.dp, 55.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.name.orEmpty(),
                fontSize = 11.sp,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RightPanel(
    title: String,
    categories: List<String>,
    selectedCategory: String?,
    items: List<ItemDetail>,
    loading: Boolean,
    error: String?,
    onCategoryClick: (String) -> Unit,
    onItemClick: (itemId: String, itemTitle: String) -> Unit,
    onAddToCart: (ItemDetail) -> Unit,
    onRemoveFromCart: (ItemDetail) -> Unit
) {
    var selectedSort by remember { mutableStateOf("Default") }

    val sortedItems = when (selectedSort) {
        "Popularity: High to Low" -> items.sortedByDescending {
            it.itemPrice?.toDoubleOrNull() ?: 0.0
        }
        else -> items
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        item {
            FilterChipsRow(
                categories = categories,
                selectedCategory = selectedCategory,
                selectedSort = selectedSort,
                onCategoryClick = onCategoryClick,
                onSortSelected = { selectedSort = it }
            )
        }

        item {
            SectionDivider(title = title)
        }

        when {
            loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF1A56C4))
                    }
                }
            }

            error != null -> {
                item {
                    Text(
                        text = "Error: $error",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No products available", color = Color.Gray)
                    }
                }
            }

            else -> {
                items(sortedItems) { item ->
                    ProductCard(
                        item = item,
                        onAddToCart = { onAddToCart(item) },
                        onRemoveFromCart = { onRemoveFromCart(item) },
                        onClick = {
                            onItemClick(item.itemId.orEmpty(), item.itemName.orEmpty())
                        }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

val sortOptions = listOf("Default", "Popularity: High to Low")

@Composable
private fun FilterChipsRow(
    categories: List<String>,
    selectedCategory: String?,
    selectedSort: String,
    onCategoryClick: (String) -> Unit,
    onSortSelected: (String) -> Unit
) {
    var showSortDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategoryClick(cat) },
                    label = {
                        Text(
                            cat.replace("kg", " kg", ignoreCase = true).replace("  ", " "),
                            fontSize = 12.sp
                        )
                    },
                    trailingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1A56C4),
                        selectedLabelColor = Color.White,
                        selectedTrailingIconColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color(0xFFBBBBBB),
                        selectedBorderColor = Color(0xFF1A56C4)
                    )
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SuggestionChip(
                onClick = { showSortDialog = true },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF333333)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Sort by: $selectedSort",
                            fontSize = 12.sp,
                            color = Color(0xFF333333)
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color.White
                )
            )
        }
    }

    if (showSortDialog) {
        Dialog(onDismissRequest = { showSortDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sort", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = { showSortDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    sortOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSortSelected(option)
                                    showSortDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSort == option,
                                onClick = {
                                    onSortSelected(option)
                                    showSortDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF1A56C4)
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(option, fontSize = 15.sp)
                        }
                        if (option != sortOptions.last()) {
                            Divider(color = Color(0xFFF0F0F0))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionDivider(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f), color = Color(0xFFCCCCCC), thickness = 1.dp)
        Text(
            text = "  $title  ",
            fontSize = 12.sp,
            color = Color(0xFF888888),
            fontWeight = FontWeight.Medium
        )
        Divider(modifier = Modifier.weight(1f), color = Color(0xFFCCCCCC), thickness = 1.dp)
    }
}

@Composable
private fun ProductCard(
    item: ItemDetail,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    onClick: () -> Unit
) {
    val entries by CartManager.entries.collectAsStateWithLifecycle()
    val qty = entries[item.itemId]?.quantity ?: 0
    val inStock = item.stock?.trim() != "0"

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Text(
                text = item.itemName.orEmpty(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${item.itemCategory.orEmpty()} kg",
                fontSize = 13.sp,
                color = Color(0xFF555555)
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Starting from",
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )
                    Text(
                        text = "₹${item.itemPrice.orEmpty()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(0xFF111111)
                    )
                    if (!item.brandName.isNullOrBlank()) {
                        Text(
                            text = item.brandName,
                            fontSize = 11.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }

                // Product image with ADD button overlaid
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F0F0))
                ) {
                    AsyncImage(
                        model = fullImageUrl(item.imgsrc),
                        contentDescription = item.itemName,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "1pc",
                        fontSize = 10.sp,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(
                                color = Color(0xFFEEEEEE),
                                shape = RoundedCornerShape(bottomEnd = 6.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    // Blue + button overlaid on image (bottom right)
                    if (inStock && qty == 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1A56C4))
                                .clickable { onAddToCart() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add to cart",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                !inStock -> {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFBBBBBB)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            disabledContentColor = Color(0xFF999999)
                        )
                    ) {
                        Text("No Stock", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                qty == 0 -> {
                    OutlinedButton(
                        onClick = { onAddToCart() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1A56C4)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A56C4))
                    ) {
                        Text("ADD", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }

                else -> {
                    // Quantity counter row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFF1A56C4), RoundedCornerShape(8.dp)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onRemoveFromCart() }) {
                            Icon(
                                Icons.Filled.Remove,
                                contentDescription = "Decrease",
                                tint = Color(0xFF1A56C4)
                            )
                        }
                        Text(
                            text = "$qty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1A56C4)
                        )
                        IconButton(onClick = { onAddToCart() }) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Increase",
                                tint = Color(0xFF1A56C4)
                            )
                        }
                    }
                }
            }
        }
    }
}
