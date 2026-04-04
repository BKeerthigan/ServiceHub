package com.example.servicehub.ui.food

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.data.model.ProductListItem
import com.example.servicehub.viewmodel.FoodViewModel
import kotlin.math.ceil

private const val BASE_IMAGE_URL = "https://jmsn.in//images//appimage//"
private val SELECTED_RED = Color(0xFFE53935)

private fun fullImageUrl(img: String?): String? {
    if (img.isNullOrBlank()) return null
    if (img.startsWith("http", true)) return img
    return BASE_IMAGE_URL + img.trim().removePrefix("/")
}

// ✅ Intent extras
private const val EXTRA_TYPE_ID = "type_id"   // existing key used to open FoodActivity
private const val EXTRA_LIST_ID = "listid"    // next screen API param
private const val EXTRA_TITLE = "title"

class FoodActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val typeId = intent.getStringExtra(EXTRA_TYPE_ID) ?: "1" // "1" FOOD, "2" FMCG


        setContent {
            FoodScreen(
                typeId = typeId,
                onBack = { finish() },
                onCategoryClick = { clicked ->
                    val listId = clicked.listId.orEmpty()
                    if (listId.isBlank()) return@FoodScreen

                    startActivity(
                        Intent(this, FoodItemListActivity::class.java).apply {
                            putExtra(EXTRA_LIST_ID, listId)
                            putExtra(EXTRA_TITLE, clicked.name.orEmpty())
                            // Optional: carry forward typeId if you want theme/bottom tab on next screen
                            putExtra(EXTRA_TYPE_ID, typeId)
                        }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodScreen(
    typeId: String,
    onBack: () -> Unit,
    onCategoryClick: (ProductListItem) -> Unit,
    vm: FoodViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(typeId) {
        vm.loadFood(typeId = typeId)
    }

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        bottomBar = {
            FoodBottomBar(
                selected = if (typeId == "2") BottomTab.FMCG else BottomTab.FOOD,
                onFood = { /* already here */ },
                onFmcg = { /* later */ },
                onCategories = { /* later */ },
                onCart = { /* later */ },
                onAccount = { /* later */ }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(if (typeId == "2") Color(0xFF0D47A1) else SELECTED_RED)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(start = 42.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (state.loading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            }

            state.error?.let { err ->
                item {
                    Text(
                        text = "Error: $err",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            state.sections.forEach { section ->
                item {
                    SectionCard(
                        title = section.title,
                        items = section.items,
                        onItemClick = onCategoryClick
                    )
                }
            }

            item { Spacer(Modifier.height(30.dp)) }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    items: List<ProductListItem>,
    onItemClick: (ProductListItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            val rows = ceil(items.size / 2f).toInt().coerceAtLeast(1)
            val itemHeight = 130.dp
            val verticalSpacing = 10.dp
            val gridHeight = (rows * itemHeight.value + (rows - 1) * verticalSpacing.value).dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false
            ) {
                items(items) { item ->
                    GridItemCard(
                        item = item,
                        onClick = onItemClick
                    )
                }
            }
        }
    }
}

@Composable
private fun GridItemCard(
    item: ProductListItem,
    onClick: (ProductListItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        modifier = Modifier
            .height(130.dp)
            .clickable { onClick(item) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AsyncImage(
                model = fullImageUrl(item.imgsrc),
                contentDescription = item.name.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Text(text = item.name.orEmpty(), style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Bottom Bar */
private enum class BottomTab { FOOD, FMCG, CATEGORIES, CART, ACCOUNT }

@Composable
private fun FoodBottomBar(
    selected: BottomTab,
    onFood: () -> Unit,
    onFmcg: () -> Unit,
    onCategories: () -> Unit,
    onCart: () -> Unit,
    onAccount: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        NavItem("Food", Icons.Filled.Store, selected == BottomTab.FOOD, onFood)
        NavItem("Categories", Icons.Filled.GridView, selected == BottomTab.CATEGORIES, onCategories)
        NavItem("Cart", Icons.Filled.ShoppingCart, selected == BottomTab.CART, onCart)
        NavItem("Account", Icons.Filled.AccountCircle, selected == BottomTab.ACCOUNT, onAccount)
    }
}

@Composable
private fun RowScope.NavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) }
    )
}